#!/usr/bin/env bash
set -euo pipefail

image=${1:?Usage: bash scripts/deploy.sh IMAGE [STAGED_RELEASE]}
staged_release=${2:-}
compose_file=compose-prod.yaml
health_url=https://api.nailroad.site/actuator/health
nginx_config=nginx/default.conf

log() {
  printf '[deploy] %s\n' "$1"
}

# CD 실행에서는 전송받은 임시 배포본의 Compose 파일을 사용합니다.
release_compose_file=$compose_file
if [[ -n "$staged_release" ]]; then
  if [[ ! "$staged_release" =~ ^\.incoming-[0-9]+-[0-9]+$ ]]; then
    echo "Invalid staging directory." >&2
    exit 1
  fi
  release_compose_file="$staged_release/$compose_file"
fi

if [[ ! -f .env || ! -f "$release_compose_file" ]]; then
  echo "Run this script from the EC2 deployment directory after the initial setup." >&2
  exit 1
fi

# 여러 배포가 동시에 운영 설정과 이미지 태그를 갱신하지 않도록 잠급니다.
log "Waiting for the deployment lock."
exec 9> .deploy.lock
flock -w 300 9

# 운영 설정이 없을 때만 기본 설정을 설치해 EC2에서 설정한 HTTPS를 보존합니다.
if [[ ! -f "$nginx_config" && -n "$staged_release" && -f "$staged_release/$nginx_config" ]]; then
  log "Installing the initial NGINX configuration."
  mkdir -p nginx
  cp "$staged_release/$nginx_config" "$nginx_config"
fi
if [[ ! -f "$nginx_config" ]]; then
  echo "Missing NGINX configuration: $nginx_config." >&2
  exit 1
fi

# 임시 Compose 파일의 .env와 NGINX 마운트 경로도 EC2 배포 디렉터리에서 찾습니다.
log "Validating the production Compose configuration."
release_compose=(docker compose --project-directory "$PWD" -f "$release_compose_file")
APP_IMAGE="$image" "${release_compose[@]}" config --quiet

# 새 컨테이너를 띄우기 전에 롤백에 사용할 현재 앱 이미지 참조를 기록합니다.
previous_container=$(APP_IMAGE="$image" "${release_compose[@]}" ps -q app)
previous_image=
if [[ -n "$previous_container" ]]; then
  previous_image=$(docker inspect --format '{{.Config.Image}}' "$previous_container")
fi

# 새 이미지 다운로드를 마친 뒤 운영 Compose 파일을 교체합니다.
log "Pulling app image $image."
APP_IMAGE="$image" "${release_compose[@]}" pull app
if [[ -n "$staged_release" ]]; then
  mkdir -p scripts
  cp "$staged_release/scripts/deploy.sh" scripts/
  mv "$release_compose_file" "$compose_file"
fi

start_and_check() {
  local APP_IMAGE=$1
  export APP_IMAGE

  log "Starting app and NGINX with app image $APP_IMAGE."
  docker compose -f "$compose_file" up -d --wait --wait-timeout 120 app nginx || return

  # 앱이 준비되면 NGINX가 새 앱 주소를 읽도록 설정 검사 후 리로드합니다.
  log "Testing the NGINX configuration."
  docker compose -f "$compose_file" exec -T nginx nginx -t || return
  log "Reloading NGINX."
  docker compose -f "$compose_file" exec -T nginx nginx -s reload || return

  # HTTPS 요청은 EC2 내부로 보내면서 도메인의 인증서와 프록시 경로를 확인합니다.
  log "Checking HTTPS health at $health_url."
  curl --fail --silent --show-error --retry 5 --retry-all-errors \
    --connect-timeout 5 --max-time 15 --output /dev/null \
    --resolve api.nailroad.site:443:127.0.0.1 "$health_url"
}

# 실패 로그를 남기고 이전 이미지가 있으면 복구를 시도한 뒤 CD에 실패를 알립니다.
if ! start_and_check "$image"; then
  log "Deployment checks failed for $image." >&2
  APP_IMAGE="$image" docker compose -f "$compose_file" logs --tail 100 app nginx >&2 || true
  if [[ -n "$previous_image" && "$previous_image" != "$image" ]]; then
    log "Restoring previous app image $previous_image."
    if start_and_check "$previous_image"; then
      echo "Restored previous app image $previous_image." >&2
    else
      echo "Failed to restore previous app image $previous_image." >&2
    fi
  fi
  exit 1
fi

# 검증에 성공하면 임시 파일을 완성한 뒤 .env를 교체해 새 APP_IMAGE를 저장합니다.
log "Saving the verified app image tag."
updated_env=$(mktemp .env.XXXXXX)
trap 'rm -f -- "$updated_env"' EXIT
sed -E '/^[[:space:]]*(export[[:space:]]+)?APP_IMAGE[[:space:]]*=/d' .env > "$updated_env"
printf '\nAPP_IMAGE=%s\n' "$image" >> "$updated_env"
mv "$updated_env" .env
log "Deployed $image."

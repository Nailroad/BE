"""Populate missing design image dimensions from S3.

Requires: pip install boto3 "psycopg[binary]" pillow
Environment: DB_URL, DB_USERNAME, DB_PASSWORD, S3_BUCKET, AWS_REGION
Run the SQL migration first. Preview with no flags; pass --apply to update rows.
"""

import argparse
from io import BytesIO
import os

import boto3
from PIL import Image
import psycopg


def required(name):
    value = os.environ.get(name)
    if not value:
        raise SystemExit(f"Missing environment variable: {name}")
    return value


def dimensions(s3, bucket, key):
    response = s3.get_object(Bucket=bucket, Key=key)
    body = response["Body"]
    try:
        content = body.read()
    finally:
        body.close()
    with Image.open(BytesIO(content)) as image:
        width, height = image.size
        # Browsers honor EXIF orientation; use the displayed dimensions.
        if image.getexif().get(274) in (5, 6, 7, 8):
            width, height = height, width
        return width, height


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--apply", action="store_true", help="Write dimensions to the database")
    parser.add_argument("--batch-size", type=int, default=100)
    args = parser.parse_args()
    if args.batch_size < 1:
        parser.error("--batch-size must be positive")

    database_url = required("DB_URL").removeprefix("jdbc:")
    bucket = required("S3_BUCKET")
    s3 = boto3.client("s3", region_name=required("AWS_REGION"))
    updated = failed = last_id = 0

    with psycopg.connect(database_url, user=required("DB_USERNAME"),
                         password=required("DB_PASSWORD")) as connection:
        while True:
            with connection.cursor() as cursor:
                cursor.execute("""
                    SELECT design_id, design_image_url
                    FROM design
                    WHERE design_id > %s
                      AND (image_width IS NULL OR image_height IS NULL)
                      AND design_image_url IS NOT NULL
                      AND design_image_url <> ''
                    ORDER BY design_id
                    LIMIT %s
                """, (last_id, args.batch_size))
                rows = cursor.fetchall()
            if not rows:
                break

            for design_id, key in rows:
                last_id = design_id
                try:
                    width, height = dimensions(s3, bucket, key)
                except Exception as error:
                    failed += 1
                    print(f"{design_id}: failed ({error})")
                    continue
                if args.apply:
                    with connection.cursor() as cursor:
                        cursor.execute("""
                            UPDATE design SET image_width = %s, image_height = %s
                            WHERE design_id = %s
                              AND (image_width IS NULL OR image_height IS NULL)
                        """, (width, height, design_id))
                updated += 1
                print(f"{design_id}: {width}x{height}")
            if args.apply:
                connection.commit()

    mode = "Updated" if args.apply else "Ready to update"
    print(f"{mode}: {updated}; failed: {failed}")


if __name__ == "__main__":
    main()

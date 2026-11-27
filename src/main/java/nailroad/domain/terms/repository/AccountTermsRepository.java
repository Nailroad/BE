package nailroad.domain.terms.repository;

import nailroad.domain.terms.entity.AccountTerms;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountTermsRepository extends JpaRepository<AccountTerms, Long> {
}

// src/main/java/org/hanseo/boongboong/domain/trust/repository/TrustPointHistoryRepo.java
package org.hanseo.boongboong.domain.trust.repository;

import org.hanseo.boongboong.domain.trust.entity.TrustPointHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrustPointHistoryRepo extends JpaRepository<TrustPointHistory, Long> {}

package meet.myo.repository;

import meet.myo.domain.Favorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    Optional<Favorite> findByIdAndDeletedAtNull(Long id);
    Page<Favorite> findByIdAndDeletedAtNull(Long memberId, Pageable pageable);
}
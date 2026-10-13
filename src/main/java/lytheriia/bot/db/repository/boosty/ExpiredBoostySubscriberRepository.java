package lytheriia.bot.db.repository.boosty;

import org.springframework.data.jpa.repository.JpaRepository;

import lytheriia.bot.db.entity.boosty.ExpiredBoostySubscriberEntity;

public interface ExpiredBoostySubscriberRepository extends JpaRepository<ExpiredBoostySubscriberEntity, Long> {

}
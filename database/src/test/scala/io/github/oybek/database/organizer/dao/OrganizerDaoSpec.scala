package io.github.oybek.database.organizer.dao

import cats.data.NonEmptyList
import cats.effect.unsafe.implicits.global
import com.dimafeng.testcontainers.scalatest.TestContainerForAll
import doobie.*
import doobie.implicits.*
import io.github.oybek.database.DB
import io.github.oybek.database.PostgresSetup
import io.github.oybek.organizer.dao.OrganizerDao
import io.github.oybek.organizer.model.Will
import org.scalatest.funsuite.AnyFunSuite

import java.time.OffsetDateTime
import java.time.ZoneOffset
import scala.concurrent.ExecutionContext.global as globalEc
import scala.concurrent.duration.DurationInt

trait OrganizerDaoSpec extends AnyFunSuite with PostgresSetup:

  val organizerDao = OrganizerDao.create

  val someTime = OffsetDateTime.of(2022, 3, 25, 9, 0, 0, 0, ZoneOffset.ofHours(5))
  val will = Will(
    chatId = 123,
    userName = "joraqul",
    userId = 123,
    hour   = someTime
  )

  test("OrganizerDao") {
    transactor.use { tx =>
      for
        _ <- organizerDao.save(NonEmptyList.of(will)).transact(tx)
        willRead <- organizerDao.findBy(will.hour).transact(tx)
        _ = assert(List(will) == willRead)
        _ <- organizerDao.deleteBefore(will.hour.minusHours(1)).transact(tx)
        willRead <- organizerDao.findBy(will.hour).transact(tx)
        _ = assert(List(will) == willRead)
        _ <- organizerDao.deleteBefore(will.hour.plusHours(1)).transact(tx)
        willRead <- organizerDao.findBy(will.hour).transact(tx)
        _ = assert(willRead.isEmpty)
      yield ()
    }.unsafeRunTimed(10.seconds)
  } 
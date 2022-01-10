package io.github.oybek.database

import cats.effect.IO
import com.dimafeng.testcontainers.{ForAllTestContainer, PostgreSQLContainer}
import doobie._
import doobie.implicits._
import io.github.oybek.database.config.DbConfig
import io.github.oybek.database.dao.BalanceDao
import io.github.oybek.database.dao.impl.BalanceDaoImpl
import io.github.oybek.database.model.Balance
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

import scala.concurrent.ExecutionContext.global

class BalanceDaoSpec extends AnyFlatSpec with ForAllTestContainer  {

  override val container: PostgreSQLContainer = PostgreSQLContainer()

  val balanceDao: BalanceDao[ConnectionIO] = BalanceDaoImpl

  "addOrUpdate" should "work" in {
    val transactor = DB.createTransactor[IO](
      DbConfig(
        container.driverClassName,
        container.jdbcUrl,
        container.username,
        container.password
      ),
      global
    )

    transactor.use { tx =>
      for {
        _ <- DB.runMigrations(tx)

        affectedRows <- balanceDao
          .addOrUpdate(Balance(telegramId = 123, seconds = 60))
          .transact(tx)
        _ = affectedRows shouldEqual 1

        balanceOpt <- balanceDao
          .findBy(telegramId = 123)
          .transact(tx)
        _ = balanceOpt shouldEqual Some(Balance(telegramId = 123, seconds = 60))

        affectedRows <- balanceDao
          .addOrUpdate(Balance(telegramId = 123, seconds = 70))
          .transact(tx)
        _ = affectedRows shouldEqual 1

        affectedRows <- balanceDao
          .addOrUpdate(Balance(telegramId = 123, seconds = 70))
          .transact(tx)
        _ = affectedRows shouldEqual 0

        balanceOpt <- balanceDao
          .findBy(telegramId = 123)
          .transact(tx)
        _ = balanceOpt shouldEqual Some(Balance(telegramId = 123, seconds = 70))

        balanceOpt <- balanceDao
          .findBy(telegramId = 0)
          .transact(tx)
        _ = balanceOpt shouldEqual None
      } yield ()
    }
  }
}


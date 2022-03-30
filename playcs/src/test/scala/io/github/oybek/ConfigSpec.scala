package io.github.oybek

import cats.data.NonEmptyList
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.github.oybek.database.DbConfig
import org.scalatest.funsuite.AnyFunSuite
import org.testcontainers.shaded.org.apache.commons.lang.SystemUtils

import java.util.Properties

class AppConfigSpec extends AnyFunSuite:

  test("Config load") {
    val properties = List(
      "database.driver" -> "org.postgresql.Driver",
      "database.url" -> "url",
      "database.user" -> "user",
      "database.pass" -> "pass",
      "server.ip" -> "SERVER_IP",
      "tg.token" -> "BOT_TOKEN",
      "hlds.dir" -> "HLDS_DIR",
      "pool.size" -> "2"
    )
    properties.map {
      case (key, value) =>
        System.setProperty(key, value)
    }

    val loadedConfig = AppConfig.create[IO].attempt.unsafeRunSync()
    val expectedConfig = Right(
      AppConfig(
        DbConfig(
          driver = "org.postgresql.Driver",
          url = "url",
          user = "user",
          pass = "pass"
          ),
        "SERVER_IP",
        "BOT_TOKEN",
        "HLDS_DIR",
        2
      )
    )
    assert(loadedConfig === expectedConfig)
  }
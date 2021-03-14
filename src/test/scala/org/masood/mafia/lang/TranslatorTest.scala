package org.masood.mafia.lang

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

class TranslatorTest extends AnyFunSuite {

  test("should be able to construct with no country") {
    val translator = new Translator("nl")
    translator.getLocale.getLanguage shouldBe "nl"
    translator.getLocale.getCountry shouldBe ""
  }

  test("should be able to fetch i18n String") {
    val translator = new Translator("fa")
    translator.getLocale.getLanguage shouldBe "fa"
    translator.getLocale.getCountry shouldBe ""
    translator.get("testMe") shouldBe "درورد! من تست هستم"
  }

  test("should be able to fetch String with country") {
    val translator = new Translator("en_UK")
    translator.getLocale.getLanguage shouldBe "en"
    translator.getLocale.getCountry shouldBe "UK"
    translator.get("testMe") shouldBe "I'm a bloody test!"
  }

  test("should be able to fetch String with another country") {
    val translator = new Translator("en_US")
    translator.getLocale.getLanguage shouldBe "en"
    translator.getLocale.getCountry shouldBe "US"
    translator.get("testMe") shouldBe "Yo! I'm a f**king test!"
  }

}

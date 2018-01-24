package utils

import org.scalatest.FunSuite

class ValidatorsSuite extends FunSuite {
  test("correct url - 1") {
    assert(Validators.isValidUrl("http://www.google.com"))
  }

  test("correct url - 2") {
    assert(Validators.isValidUrl("www.google.com"))
  }

  test("correct url - 3") {
    assert(Validators.isValidUrl("http://google.com/next/next"))
  }

  test("correct url - 4") {
    assert(Validators.isValidUrl("https://google.com/next/next"))
  }

  test("correct url - 5") {
    assert(Validators.isValidUrl("http://localhost.com/a/b/c"))
  }

  test("correct url - 6") {
    assert(Validators.isValidUrl("http://localhost/sds/a/a/"))
  }

  test("correct url - 7") {
    assert(Validators.isValidUrl("http://localhost:9000/sds/a/a/"))
    assert(Validators.isValidUrl("http://localhost:90000/sds/a/a/"))
  }

  test("wrong url - 1") {
    assert(!Validators.isValidUrl("htt://google.com/next/next"))
  }

  test("wrong url - 2") {
    assert(!Validators.isValidUrl("ww://google.com/next/next"))
  }

  test("wrong url - 3") {
    assert(!Validators.isValidUrl(""))
  }
}
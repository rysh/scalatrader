package domain

package object user {
  case class Settings(name: String, key: String, secret: String)
}

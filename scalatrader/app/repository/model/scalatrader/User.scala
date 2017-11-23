package repository.model.scalatrader

case class User(id:Long,
                email:String,
                password:String,
                name:String,
                api_key:String,
                api_secret:String)
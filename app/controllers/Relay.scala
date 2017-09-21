package controllers

import play.api.mvc._

import lila.api.Context
import lila.app._
import lila.relay.{ Relay => RelayModel }
import views._

object Relay extends LilaController {

  private val env = Env.relay

  def index = Open { implicit ctx =>
    env.api.all map { sel =>
      Ok(html.relay.index(sel))
    }
  }

  def form = Auth { implicit ctx => me =>
    NoLame {
      Ok(html.relay.create(env.forms.create)).fuccess
    }
  }

  def create = AuthBody { implicit ctx => implicit me =>
    implicit val req = ctx.body
    env.forms.create.bindFromRequest.fold(
      err => BadRequest(html.relay.create(err)).fuccess,
      setup => env.api.create(setup, me) map { relay =>
        Redirect(routes.Relay.show(relay.id.value))
      }
    )
  }

  def show(id: String) = Open { implicit ctx =>
    OptionOk(env.api byId RelayModel.Id(id)) { relay =>
      html.relay.show(relay)
    }
  }
}
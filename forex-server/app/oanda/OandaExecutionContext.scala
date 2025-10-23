package oanda

import javax.inject._
import org.apache.pekko.actor.ActorSystem
import play.api.libs.concurrent.CustomExecutionContext

@Singleton
class OandaExecutionContext @Inject() (system: ActorSystem)
    extends CustomExecutionContext(system, "oanda.dispatcher")

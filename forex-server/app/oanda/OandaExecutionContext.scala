package oanda

import javax.inject._
import org.apache.pekko.actor.ActorSystem
import play.api.libs.concurrent.CustomExecutionContext

/** Custom ExecutionContext used for sending requests to Oanda with a specific
  * thread pool (to prevent blocking in the CandleController). The @Singleton
  * decorator is used because we only want to ever use the same instance of this
  * thread pool when sending requests to Oanda.
  *
  * Note that "oanda.dispatcher" corresponds to the name given in the
  * application.conf file (for configuring the thread pool).
  *
  * @param system
  *   Pekko ActorSystem (needed for extending the CustomExecutionContext class).
  */
@Singleton
class OandaExecutionContext @Inject() (system: ActorSystem)
    extends CustomExecutionContext(system, "oanda.dispatcher")

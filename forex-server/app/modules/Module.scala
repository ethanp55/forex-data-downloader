package modules

import com.google.inject.{AbstractModule, Provides, Singleton}
import sttp.client4.{DefaultSyncBackend, SyncBackend}

/** Simple class to provide a SyncBackend instance when it is requested with the @Inject
  * decorator.
  */
class Module extends AbstractModule {
  @Provides
  @Singleton
  def provideSyncBackend(): SyncBackend = {
    DefaultSyncBackend()
  }
}

package modules

import com.google.inject.{AbstractModule, Provides, Singleton}
import sttp.client4.{DefaultSyncBackend, SyncBackend}

class Module extends AbstractModule {
  @Provides
  @Singleton
  def provideSyncBackend(): SyncBackend = {
    DefaultSyncBackend()
  }
}

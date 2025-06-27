package io.github.samuelmarks.off_on_ml

import com.russhwolf.settings.Settings

val settings: Settings = Settings()

object SettingsKeys {
    const val SELECTED_OFFLINE_MODEL = "google/medgemma-4b-it"
    const val SELECTED_ONLINE_MODEL = "gemini-2.5-flash"
    /* `.first()` / `[0]` doesn't work for `const`s; so just some literals ^
     *  could probably hack gradle to generate these in future… */

    const val GOOGLE_API_KEY = ""
}

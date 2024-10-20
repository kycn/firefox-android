/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package mozilla.components.browser.state.reducer

import mozilla.components.browser.state.action.TranslationsAction
import mozilla.components.browser.state.selector.findTab
import mozilla.components.browser.state.state.BrowserState
import mozilla.components.browser.state.state.SessionState
import mozilla.components.browser.state.state.TranslationsState
import mozilla.components.concept.engine.translate.TranslationOperation
import mozilla.components.concept.engine.translate.TranslationPageSettingOperation
import mozilla.components.concept.engine.translate.TranslationPageSettings

internal object TranslationsStateReducer {

    /**
     * Reducer for [BrowserState.translationEngine] and [SessionState.translationsState]
     */
    @Suppress("LongMethod")
    fun reduce(state: BrowserState, action: TranslationsAction): BrowserState = when (action) {
        is TranslationsAction.TranslateExpectedAction -> {
            state.copyWithTranslationsState(action.tabId) {
                it.copy(
                    isExpectedTranslate = true,
                )
            }
        }

        is TranslationsAction.TranslateOfferAction -> {
            state.copyWithTranslationsState(action.tabId) {
                it.copy(
                    isOfferTranslate = true,
                )
            }
        }

        is TranslationsAction.TranslateStateChangeAction -> {
            if (action.translationEngineState.requestedTranslationPair == null ||
                action.translationEngineState.requestedTranslationPair?.fromLanguage == null ||
                action.translationEngineState.requestedTranslationPair?.toLanguage == null
            ) {
                state.copyWithTranslationsState(action.tabId) {
                    it.copy(
                        isTranslated = false,
                        translationEngineState = action.translationEngineState,
                    )
                }
            } else {
                state.copyWithTranslationsState(action.tabId) {
                    it.copy(
                        isTranslated = true,
                        translationError = null,
                        translationEngineState = action.translationEngineState,
                    )
                }
            }
        }

        is TranslationsAction.TranslateAction ->
            state.copyWithTranslationsState(action.tabId) {
                it.copy(isTranslateProcessing = true)
            }

        is TranslationsAction.TranslateRestoreAction ->
            state.copyWithTranslationsState(action.tabId) {
                it.copy(isRestoreProcessing = true)
            }

        is TranslationsAction.TranslateSuccessAction -> {
            when (action.operation) {
                TranslationOperation.TRANSLATE -> {
                    state.copyWithTranslationsState(action.tabId) {
                        it.copy(
                            isTranslated = true,
                            isTranslateProcessing = false,
                            translationError = null,
                        )
                    }
                }

                TranslationOperation.RESTORE -> {
                    state.copyWithTranslationsState(action.tabId) {
                        it.copy(
                            isTranslated = false,
                            isRestoreProcessing = false,
                            translationError = null,
                        )
                    }
                }

                TranslationOperation.FETCH_SUPPORTED_LANGUAGES -> {
                    // Reset the error state, and then generally expect
                    // [TranslationsAction.SetSupportedLanguagesAction] to update state in the
                    // success case.
                    state.copyWithTranslationsState(action.tabId) {
                        it.copy(
                            translationError = null,
                        )
                    }
                }

                TranslationOperation.FETCH_PAGE_SETTINGS -> {
                    // Reset the error state, and then generally expect
                    // [TranslationsAction.SetPageSettingsAction] to update state in the
                    // success case.
                    state.copyWithTranslationsState(action.tabId) {
                        it.copy(
                            settingsError = null,
                        )
                    }
                }

                TranslationOperation.FETCH_NEVER_TRANSLATE_SITES -> {
                    // Reset the error state, and then generally expect
                    // [TranslationsAction.SetNeverTranslateSitesAction] to update
                    // state in the success case.
                    state.copyWithTranslationsState(action.tabId) {
                        it.copy(
                            neverTranslateSites = null,
                        )
                    }
                }
            }
        }

        is TranslationsAction.TranslateExceptionAction -> {
            when (action.operation) {
                TranslationOperation.TRANSLATE -> {
                    state.copyWithTranslationsState(action.tabId) {
                        it.copy(
                            isTranslateProcessing = false,
                            translationError = action.translationError,
                        )
                    }
                }

                TranslationOperation.RESTORE -> {
                    state.copyWithTranslationsState(action.tabId) {
                        it.copy(
                            isRestoreProcessing = false,
                            translationError = action.translationError,
                        )
                    }
                }

                TranslationOperation.FETCH_SUPPORTED_LANGUAGES -> {
                    state.copyWithTranslationsState(action.tabId) {
                        it.copy(
                            supportedLanguages = null,
                            translationError = action.translationError,
                        )
                    }
                }

                TranslationOperation.FETCH_PAGE_SETTINGS -> {
                    state.copyWithTranslationsState(action.tabId) {
                        it.copy(
                            pageSettings = null,
                            settingsError = action.translationError,
                        )
                    }
                }

                TranslationOperation.FETCH_NEVER_TRANSLATE_SITES -> {
                    state.copyWithTranslationsState(action.tabId) {
                        it.copy(
                            neverTranslateSites = null,
                            settingsError = action.translationError,
                        )
                    }
                }
            }
        }

        is TranslationsAction.EngineExceptionAction -> {
            state.copy(translationEngine = state.translationEngine.copy(engineError = action.error))
        }

        is TranslationsAction.SetSupportedLanguagesAction ->
            state.copyWithTranslationsState(action.tabId) {
                it.copy(
                    supportedLanguages = action.supportedLanguages,
                    translationError = null,
                )
            }

        is TranslationsAction.SetPageSettingsAction ->
            state.copyWithTranslationsState(action.tabId) {
                it.copy(
                    pageSettings = action.pageSettings,
                    settingsError = null,
                )
            }

        is TranslationsAction.SetNeverTranslateSitesAction ->
            state.copyWithTranslationsState(action.tabId) {
                it.copy(
                    neverTranslateSites = action.neverTranslateSites,
                )
            }

        is TranslationsAction.RemoveNeverTranslateSiteAction -> {
            val neverTranslateSites = state.findTab(action.tabId)?.translationsState?.neverTranslateSites
            val updatedNeverTranslateSites = neverTranslateSites?.filter { it != action.origin }?.toList()
            state.copyWithTranslationsState(action.tabId) {
                it.copy(
                    neverTranslateSites = updatedNeverTranslateSites,
                )
            }
        }

        is TranslationsAction.OperationRequestedAction ->
            when (action.operation) {
                TranslationOperation.FETCH_SUPPORTED_LANGUAGES -> {
                    state.copyWithTranslationsState(action.tabId) {
                        it.copy(
                            supportedLanguages = null,
                        )
                    }
                }
                TranslationOperation.FETCH_PAGE_SETTINGS -> {
                    state.copyWithTranslationsState(action.tabId) {
                        it.copy(
                            pageSettings = null,
                        )
                    }
                }
                TranslationOperation.FETCH_NEVER_TRANSLATE_SITES -> {
                    state.copyWithTranslationsState(action.tabId) {
                        it.copy(
                            neverTranslateSites = null,
                        )
                    }
                }
                TranslationOperation.TRANSLATE, TranslationOperation.RESTORE -> {
                    // No state change for these operations
                    state
                }
            }

        is TranslationsAction.UpdatePageSettingAction -> {
            var pageSettings = state.findTab(action.tabId)?.translationsState?.pageSettings
            // Initialize page settings, if null.
            if (pageSettings == null) {
                pageSettings = TranslationPageSettings()
            }
            when (action.operation) {
                TranslationPageSettingOperation.UPDATE_ALWAYS_OFFER_POPUP -> {
                    pageSettings.alwaysOfferPopup = action.setting
                    state.copyWithTranslationsState(action.tabId) {
                        it.copy(
                            pageSettings = pageSettings,
                        )
                    }
                }
                TranslationPageSettingOperation.UPDATE_ALWAYS_TRANSLATE_LANGUAGE -> {
                    pageSettings.alwaysTranslateLanguage = action.setting
                    // Always and never translate sites are always opposites.
                    pageSettings.neverTranslateLanguage = !action.setting

                    state.copyWithTranslationsState(action.tabId) {
                        it.copy(
                            pageSettings = pageSettings,
                        )
                    }
                }
                TranslationPageSettingOperation.UPDATE_NEVER_TRANSLATE_LANGUAGE -> {
                    pageSettings.neverTranslateLanguage = action.setting
                    // Always and never translate sites are always opposites.
                    pageSettings.alwaysTranslateLanguage = !action.setting
                    state.copyWithTranslationsState(action.tabId) {
                        it.copy(
                            pageSettings = pageSettings,
                        )
                    }
                }
                TranslationPageSettingOperation.UPDATE_NEVER_TRANSLATE_SITE -> {
                    pageSettings.neverTranslateSite = action.setting
                    state.copyWithTranslationsState(action.tabId) {
                        it.copy(
                            pageSettings = pageSettings,
                        )
                    }
                }
            }
        }

        is TranslationsAction.SetEngineSupportedAction -> {
            state.copy(
                translationEngine = state.translationEngine.copy(
                    isEngineSupported = action.isEngineSupported,
                    engineError = null,
                ),
            )
        }

        is TranslationsAction.FetchTranslationDownloadSizeAction -> {
            state.copyWithTranslationsState(action.tabId) {
                it.copy(
                    translationDownloadSize = null,
                )
            }
        }

        is TranslationsAction.SetTranslationDownloadSizeAction -> {
            state.copyWithTranslationsState(action.tabId) {
                it.copy(
                    translationDownloadSize = action.translationSize,
                )
            }
        }
    }

    private inline fun BrowserState.copyWithTranslationsState(
        tabId: String,
        crossinline update: (TranslationsState) -> TranslationsState,
    ): BrowserState {
        return updateTabOrCustomTabState(tabId) { current ->
            current.createCopy(translationsState = update(current.translationsState))
        }
    }
}

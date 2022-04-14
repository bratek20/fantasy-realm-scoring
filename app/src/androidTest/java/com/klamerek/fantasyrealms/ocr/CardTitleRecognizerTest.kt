package com.klamerek.fantasyrealms.ocr

import android.graphics.Bitmap
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.klamerek.fantasyrealms.game.*
import com.klamerek.fantasyrealms.getBitmapFromTestAssets
import com.klamerek.fantasyrealms.util.Language
import com.klamerek.fantasyrealms.util.LocaleManager
import com.klamerek.fantasyrealms.util.LocaleManager.english
import com.klamerek.fantasyrealms.util.LocaleManager.french
import com.klamerek.fantasyrealms.util.LocaleManager.german
import com.klamerek.fantasyrealms.util.LocaleManager.hungarian
import com.klamerek.fantasyrealms.util.LocaleManager.polish
import com.klamerek.fantasyrealms.util.LocaleManager.russian
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@MediumTest
class CardTitleRecognizerTest {

    @BeforeEach
    fun beforeEach() {
        GrantPermissionRule.grant(android.Manifest.permission.CAMERA)
    }

    @DisplayName("Empty image")
    @Test
    fun empty_image() {
        val bean = CardTitleRecognizer(InstrumentationRegistry.getInstrumentation().targetContext)
        val conf = Bitmap.Config.ARGB_8888
        val bitmap = Bitmap.createBitmap(400, 400, conf)
        val task = bean.process(InputImage.fromBitmap(bitmap, 0))

        Tasks.await(task)

        assertThat(task.result, Matchers.empty())
    }

    @DisplayName("Hand example, each card separated (english)")
    @Test
    fun hand_example_each_card_separated() {
        check("cardsExample1.jpg", english,
            listOf(beastmaster, forge, unicorn, princess, bellTower, bookOfChanges, candle))
    }

    @DisplayName("Hand example 2, each card separated (english)")
    @Test
    fun hand_example_2_each_card_separated() {
        check("cardsExample2.jpg", english,
            listOf(rainstorm, waterElemental, whirlwind,
                basilisk, worldTree, airElemental, wildfire, swamp, bellTower, candle,
                greatFlood, lightning //Great flood and lighting are false positives but hard to detect
        ))
    }

    @DisplayName("Hand example 3, each card separated (english)")
    @Test
    fun hand_example_3_each_card_separated() {
        check("cardsExample3.jpg", english,
            listOf(shieldOfKeth, swordOfKeth, empress,
                celestialKnights, mirage, fireElemental, forge))
    }

    @DisplayName("Title only (french)")
    @Test
    fun title_only_french() {
        check("cardExampleFrench.png", french,
            listOf(mirage, doppelganger, unicorn, worldTree, king, swamp))
    }

    @DisplayName("One card (russian)")
    @Test
    fun one_card_russian() {
        check("cardExampleRussian.jpg", russian, listOf(hydra))
    }

    @DisplayName("Card set russian")
    @Test
    fun card_set_russian() {
        check("cardSetRussian.jpg", russian,
            listOf(bellTower, protectionRune, wildfire, king, warlockLord, lightCavalry, worldTree, basilisk))
    }

    @DisplayName("Card german jester")
    @Test
    fun card_german_jester() {
        check("germanJester.jpg", german, listOf(jester))
    }

    @DisplayName("Card hungarian")
    @Test
    fun card_hungarian() {
        check("cardHungarian.jpg", hungarian, listOf(empress))
    }

    @DisplayName("Original Card Example Polish")
    @Test
    fun original_card_example_polish() {
        check("originalCardExamplePolish.jpg", polish, listOf(warDirigible))
    }

    @DisplayName("Original Hand Example Polish")
    @Test
    fun original_hand_example_polish() {
        check("originalHandExamplePolish.jpg", polish, listOf(bookOfChanges, elvenArchers, wildfire, forge, princess, fireElemental, king))
    }

    @DisplayName("Expansion Card Example Polish")
    @Test
    fun expansion_card_example_polish() {
        check("expansionCardExamplePolish.jpg", polish, listOf(genie))
    }

    @DisplayName("Expansion Hand Example Polish")
    @Test
    fun expansion_hand_example_polish() {
        check("expansionHandExamplePolish.jpg", polish, listOf(specter, dungeon, darkQueen, chapel, demon, bellTowerV2, leprechaun, dwarvishInfantry))
    }

    private fun check(fileName: String, language: Language, cards: Collection<CardDefinition>) {
        LocaleManager.saveLanguageInPreferences(InstrumentationRegistry.getInstrumentation().targetContext, language)
        val context = LocaleManager.updateContextWithPreferredLanguage(
            InstrumentationRegistry.getInstrumentation().targetContext, language
        )
        val bean = CardTitleRecognizer(context)
        val task = bean.process(InputImage.fromBitmap(getBitmapFromTestAssets(fileName), 0))

        Tasks.await(task)

        val cardIds = cards.stream().map { c -> c.id }.toArray()
        assertThat(
            task.result, Matchers.containsInAnyOrder(
                *cardIds
            )
        )
    }

}

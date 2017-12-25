package pl.margoj.editor2.app.controller

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import javafx.beans.binding.Bindings
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import pl.margoj.editor2.editor.npc.NpcEditor
import pl.margoj.editor2.editor.operation.LoadResourceOperation
import pl.margoj.mrf.MRFIcon
import pl.margoj.mrf.MargoResource
import pl.margoj.mrf.graphics.GraphicDeserializer
import pl.margoj.mrf.graphics.GraphicResource
import pl.margoj.mrf.npc.*
import pl.margoj.utils.commons.time.TimeFormatUtils
import pl.margoj.utils.javafx.api.CustomController
import pl.margoj.utils.javafx.api.CustomScene
import pl.margoj.utils.javafx.utils.FXUtils
import pl.margoj.utils.javafx.utils.QuickAlert
import java.io.ByteArrayInputStream
import java.net.URL
import java.util.*

class NpcController : CustomController
{
    private var graphicsLoaded = false
    lateinit var scene: CustomScene<*>
    lateinit var editor: NpcEditor
    var npc: MargoNpc? = null
    var icon: String = ""

    @FXML
    lateinit var tabsPane: TabPane

    @FXML
    lateinit var tabNpc: Tab

    @FXML
    lateinit var tabGraphics: Tab

    @FXML
    lateinit var tabStats: Tab

    @FXML
    lateinit var fieldId: TextField

    @FXML
    lateinit var fieldName: TextField

    @FXML
    lateinit var fieldLevel: TextField

    @FXML
    lateinit var buttonSpawnToggle: ToggleButton

    @FXML
    lateinit var fieldSpawn: TextField

    @FXML
    lateinit var comboType: ComboBox<String>

    @FXML
    lateinit var comboRank: ComboBox<String>

    @FXML
    lateinit var comboGender: ComboBox<String>

    @FXML
    lateinit var comboProfession: ComboBox<String>

    @FXML
    lateinit var fieldScript: TextField

    @FXML
    lateinit var graphicPreview: ImageView

    @FXML
    lateinit var buttonSelectGraphics: Button

    @FXML
    lateinit var statStr: TextField

    @FXML
    lateinit var statAgi: TextField

    @FXML
    lateinit var statInt: TextField

    @FXML
    lateinit var statHp: TextField

    @FXML
    lateinit var statAttackSpeed: TextField

    @FXML
    lateinit var statAttack: TextField

    @FXML
    lateinit var statArmor: TextField

    @FXML
    lateinit var statBlock: TextField

    @FXML
    lateinit var statEvade: TextField

    @FXML
    lateinit var buttonSave: Button

    override fun preInit(scene: CustomScene<*>)
    {
        this.scene = scene

        scene.stage.setOnCloseRequest {
            val result = QuickAlert.create()
                    .confirmation()
                    .header("Czy chcesz zapisać?")
                    .content("Czy chcesz zapisać aktualnego NPC?")
                    .buttonTypes(ButtonType("Tak", ButtonBar.ButtonData.YES), ButtonType("Nie", ButtonBar.ButtonData.NO), ButtonType("Anuluj", ButtonBar.ButtonData.CANCEL_CLOSE))
                    .showAndWait()

            when (result?.buttonData)
            {
                ButtonBar.ButtonData.YES ->
                {
                    this.buttonSave.fire()
                }
                ButtonBar.ButtonData.NO ->
                {
                }
                else -> it.consume()
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun loadData(data: Any)
    {
        data as Pair<NpcEditor, MargoNpc?>

        this.editor = data.first
        this.npc = data.second

        this.fieldId.text = this.npc?.id ?: ""

        fun <T> setCombo(combo: ComboBox<String>, map: BiMap<String, T>, value: T?)
        {
            combo.items = FXCollections.observableList(map.keys.toList())
            if (value == null)
            {
                combo.selectionModel.select(0)
            }
            else
            {
                combo.selectionModel.select(map.inverse()[value])
            }
        }

        setCombo(this.comboType, TYPE_MAP, this.npc?.type)
        setCombo(this.comboRank, RANK_MAP, this.npc?.rank)
        setCombo(this.comboGender, GENDER_MAP, this.npc?.gender)
        setCombo(this.comboProfession, PROFESSION_MAP, this.npc?.profession)

        if (this.npc != null)
        {
            this.fieldId.isDisable = true

            val npc = this.npc!!

            this.fieldName.text = npc.name
            this.fieldLevel.text = npc.level.toString()
            this.fieldScript.text = npc.script

            this.statStr.text = npc.strength.toString()
            this.statAgi.text = npc.agility.toString()
            this.statInt.text = npc.intellect.toString()
            this.statHp.text = npc.maxHp.toString()
            this.statAttackSpeed.text = npc.attackSpeed.toString()
            this.statAttack.text = "${npc.attack.first}-${npc.attack.endInclusive}"
            this.statArmor.text = npc.armor.toString()
            this.statBlock.text = npc.block.toString()
            this.statEvade.text = npc.evade.toString()

            this.buttonSpawnToggle.isSelected = npc.spawnTime != 0L
            this.fieldSpawn.text = if(npc.spawnTime == 0L) "" else TimeFormatUtils.toParsableTime(npc.spawnTime)

            this.icon = npc.graphics
        }
    }

    override fun initialize(location: URL?, resources: ResourceBundle?)
    {
        FXUtils.makeNumberField(this.fieldLevel, false)
        FXUtils.makeNumberField(this.statStr, false)
        FXUtils.makeNumberField(this.statAgi, false)
        FXUtils.makeNumberField(this.statInt, false)
        FXUtils.makeNumberField(this.statHp, false)
        FXUtils.makeNumberField(this.statAttackSpeed, false)
        FXUtils.makeNumberField(this.statArmor, false)
        FXUtils.makeNumberField(this.statBlock, false)
        FXUtils.makeNumberField(this.statEvade, false)
        this.statAttack.text = "0-0"

        this.fieldSpawn.disableProperty().bind(this.buttonSpawnToggle.selectedProperty().not())
        this.buttonSpawnToggle.textProperty().bind(Bindings.`when`(this.buttonSpawnToggle.selectedProperty()).then("Niestandardowy").otherwise("Domyślny"))

        this.tabsPane.selectionModel.selectedItemProperty().addListener { _, _, newValue ->
            if (!this.graphicsLoaded && newValue == this.tabGraphics)
            {
                val iconId = this.editor.editor.iconPathToIconId(GraphicResource.GraphicCategory.NPC, this.icon)
                val view = this.editor.editor.bundle.getResource(MargoResource.Category.GRAPHIC, iconId) ?: return@addListener

                this.editor.editor.startOperation(LoadResourceOperation(this.editor.editor, view, GraphicDeserializer(), { icon ->
                    this.loadGraphics(icon!!.icon)
                }))
            }
        }

        this.buttonSelectGraphics.setOnAction {
            this.editor.editor.openGraphicsChoice(GraphicResource.GraphicCategory.NPC, { graphics ->
                this.icon = this.editor.editor.createIconText(graphics)

                this.loadGraphics(graphics.icon)
            })
        }

        this.buttonSave.setOnAction {
            val errors = ArrayList<String>()

            if (this.fieldId.text.isEmpty())
            {
                errors.add("ID npc nie moze byc puste")
            }

            if (!MargoResource.ID_PATTERN.matcher(this.fieldId.text).matches())
            {
                errors.add("ID może zawierać tylko znaki alfanumeryczne i _")
            }

            if (this.fieldId.text.length > 127)
            {
                errors.add("ID npc nie moze przekraczac 127 znakow")
            }

            this.checkRange(errors, "Poziom", this.fieldLevel, 0, Int.MAX_VALUE)
            this.checkRange(errors, "Siła", this.statStr, 0, Int.MAX_VALUE)
            this.checkRange(errors, "Zręczność", this.statAgi, 0, Int.MAX_VALUE)
            this.checkRange(errors, "Intelekt", this.statInt, 0, Int.MAX_VALUE)
            this.checkRange(errors, "HP", this.statHp, 0, Int.MAX_VALUE)
            this.checkRange(errors, "SA", this.statAttackSpeed, 0, Int.MAX_VALUE)
            this.checkRange(errors, "Pancerz", this.statArmor, 0, Int.MAX_VALUE)
            this.checkRange(errors, "Blok", this.statBlock, 0, Int.MAX_VALUE)
            this.checkRange(errors, "Unik", this.statEvade, 0, Int.MAX_VALUE)

            val attackSplit = this.statAttack.text.split("-")
            var attackMin = -1
            var attackMax = -1

            if (attackSplit.size == 2)
            {
                attackMin = attackSplit[0].toIntOrNull() ?: -1
                attackMax = attackSplit[1].toIntOrNull() ?: -1
            }

            if (attackMin == -1 || attackMax == -1)
            {
                errors.add("Atak musi być zapisany jako zakres min-max. Przykład: 253-370")
            }

            var customSpawnTime: Long = 0

            if (this.buttonSpawnToggle.isSelected)
            {
                customSpawnTime = TimeFormatUtils.parseTime(this.fieldSpawn.text)

                if (customSpawnTime == 0L)
                {
                    errors.add("Podany czas jest niepoprawny! Prawidłowy format: np. 6m20s (6 minut 20 sekund)")
                }
            }

            if (errors.size > 0)
            {
                FXUtils.showMultipleErrorsAlert("Wystąpił bład podczas tworzenia nowego NPC", errors)
                return@setOnAction
            }

            val npc = this.npc ?: MargoNpc(NpcSerializer.VERSION, this.fieldId.text, this.fieldName.text)

            npc.name = this.fieldName.text
            npc.level = this.fieldLevel.text.toInt()
            npc.type = TYPE_MAP[this.comboType.selectionModel.selectedItem]!!
            npc.rank = RANK_MAP[this.comboRank.selectionModel.selectedItem]!!
            npc.gender = GENDER_MAP[this.comboGender.selectionModel.selectedItem]!!
            npc.profession = PROFESSION_MAP[this.comboProfession.selectionModel.selectedItem]!!
            npc.script = this.fieldScript.text

            npc.graphics = this.icon

            npc.strength = this.statStr.text.toInt()
            npc.agility = this.statAgi.text.toInt()
            npc.intellect = this.statInt.text.toInt()
            npc.maxHp = this.statHp.text.toInt()
            npc.attackSpeed = this.statAttackSpeed.text.toInt()
            npc.attack = attackMin..attackMax
            npc.armor = this.statArmor.text.toInt()
            npc.block = this.statBlock.text.toInt()
            npc.evade = this.statEvade.text.toInt()

            npc.spawnTime = customSpawnTime

            this.editor.editor.bundle.saveResource(npc, ByteArrayInputStream(NpcSerializer().serialize(npc)))
            this.editor.editor.reloadResourceIndex()

            QuickAlert.create().information().header("Zapisano!").content("NPC ${npc.id} został zapisany poprawnie!").showAndWait()

            this.scene.stage.close()
        }
    }

    private fun checkRange(errors: MutableList<String>, stat: String, number: TextField, min: Int, max: Int)
    {
        val i = number.text.toLong()

        if (i < min || i > max)
        {
            errors.add("$stat musi mieścić się w przedziale $min-$max")
        }
    }

    private fun loadGraphics(icon: MRFIcon)
    {
        this.graphicPreview.image = Image(ByteArrayInputStream(icon.image))
        this.graphicsLoaded = true
    }

    private companion object
    {
        val TYPE_MAP = HashBiMap.create<String, NpcType>()!!
        val RANK_MAP = HashBiMap.create<String, NpcRank>()!!
        val GENDER_MAP = HashBiMap.create<String, NpcGender>()!!
        val PROFESSION_MAP = HashBiMap.create<String, NpcProfession>()!!

        init
        {
            TYPE_MAP.put("Npc", NpcType.NPC)
            TYPE_MAP.put("Potwór", NpcType.MONSTER)

            RANK_MAP.put("Zwykły", NpcRank.NORMAL)
            RANK_MAP.put("Elita ", NpcRank.ELITE)
            RANK_MAP.put("Elita II", NpcRank.ELITE_II)
            RANK_MAP.put("Elita III", NpcRank.ELITE_III)
            RANK_MAP.put("Heros", NpcRank.HERO)
            RANK_MAP.put("Tytan", NpcRank.TITAN)

            GENDER_MAP.put("Nieokreślona", NpcGender.UNKNOWN)
            GENDER_MAP.put("Mężczyzna", NpcGender.MALE)
            GENDER_MAP.put("Kobieta", NpcGender.FEMALE)

            PROFESSION_MAP.put("Wojownik", NpcProfession.WARRIOR)
            PROFESSION_MAP.put("Paladyn", NpcProfession.PALADIN)
            PROFESSION_MAP.put("Mag", NpcProfession.MAGE)
            PROFESSION_MAP.put("Łowca", NpcProfession.HUNTER)
            PROFESSION_MAP.put("Tropiciel", NpcProfession.TRACKER)
            PROFESSION_MAP.put("Tancerz ostrzy", NpcProfession.BLADE_DANCER)
        }
    }
}
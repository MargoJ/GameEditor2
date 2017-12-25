package pl.margoj.editor2.editor.gui

import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode
import javafx.scene.input.MouseButton
import javafx.util.Callback
import pl.margoj.editor2.EditorApplication
import pl.margoj.utils.javafx.api.CustomScene
import pl.margoj.editor2.app.controller.EditorController
import pl.margoj.utils.javafx.utils.FXUtils
import pl.margoj.editor2.editor.MargoJEditor
import pl.margoj.editor2.editor.gui.map.LayerBar
import pl.margoj.editor2.editor.gui.map.MapGUI
import pl.margoj.editor2.editor.gui.map.ToolsBar
import pl.margoj.mrf.MargoResource
import pl.margoj.mrf.ResourceView
import java.util.*

class EditorGUI(val editor: MargoJEditor)
{
    lateinit var controller: EditorController
        private set

    lateinit var scene: CustomScene<*>
        private set

    val mapGUI = MapGUI(this)
    val layerBar = LayerBar(this)
    val toolsBar = ToolsBar(this)

    private lateinit var mapsItem: TreeItem<CustomText>
    private lateinit var itemsItem: TreeItem<CustomText>
    private lateinit var npcsItem: TreeItem<CustomText>
    private lateinit var scriptsItem: TreeItem<CustomText>

    fun init(scene: CustomScene<*>, controller: EditorController)
    {
        this.scene = scene
        this.controller = controller

        controller.indexRefresh.setOnAction {
            this.editor.reloadResourceIndex()
        }

        controller.tilesetCanvasScroll.setOnMousePressed { event ->
            FXUtils.pannableOnScrollclick(event, controller.tilesetCanvasScroll)
        }

        controller.tilesetCanvasScroll.setOnMouseReleased { event ->
            FXUtils.pannableOnScrollclick(event, controller.tilesetCanvasScroll)
        }

        // resources index
        val list = this.editor.gui.controller.indexList

        // game
        val gameContextMenu = ContextMenu()
        val openGameSettingsItem = MenuItem("Otwórz ustawienia gry")
        openGameSettingsItem.setOnAction {
            this.editor.openGameSettings()
        }

        gameContextMenu.items.setAll(openGameSettingsItem)

        val gameItem = TreeItem(CustomText("Gra", ImageView(FXUtils.loadIcon("list/game.png")), gameContextMenu, openGameSettingsItem))

        // maps
        val mapContextMenu = ContextMenu()
        val addNewMapItem = MenuItem("Dodaj nową mapę")
        addNewMapItem.setOnAction {
            this.editor.mapEditor.requestNewMap()
        }
        mapContextMenu.items.add(addNewMapItem)

        mapsItem = TreeItem(CustomText("Mapy", ImageView(FXUtils.loadIcon("list/world.png")), mapContextMenu))

        // items
        val itemContextMenu = ContextMenu()
        val addNewItemItem = MenuItem("Dodaj nowy przedmiot")
        addNewItemItem.setOnAction {
            this.editor.itemEditor.requestNewItem()
        }
        itemContextMenu.items.add(addNewItemItem)
        itemsItem = TreeItem(CustomText("Przedmioty", ImageView(FXUtils.loadIcon("list/bag.png")), itemContextMenu))

        // npcs
        val npcsContextMenu = ContextMenu()
        val addNewNpcItem = MenuItem("Dodaj nowego NPC")
        addNewNpcItem.setOnAction {
            this.editor.npcEditor.requestNewNpc()
        }
        npcsContextMenu.items.add(addNewNpcItem)
        npcsItem = TreeItem(CustomText("NPC", ImageView(FXUtils.loadIcon("list/npcs.png")), npcsContextMenu))

        // scripts
        val scriptsContextMenu = ContextMenu()
        val addNewScriptItem = MenuItem("Dodaj nowy skrypt")
        addNewScriptItem.setOnAction {
            this.editor.scriptEditor.requestNewScript()
        }
        scriptsContextMenu.items.add(addNewScriptItem)

        scriptsItem = TreeItem(CustomText("Skrypty", ImageView(FXUtils.loadIcon("list/scripts.png")), scriptsContextMenu))

        list.cellFactory = CustomCellFactory()

        list.setOnKeyReleased {
            if (it.code == KeyCode.DELETE)
            {
                list.selectionModel?.selectedItem?.value?.deleteAction?.fire()
            }
        }

        val root = TreeItem<CustomText>()
        root.children.add(gameItem)
        root.children.add(mapsItem)
        root.children.add(itemsItem)
        root.children.add(npcsItem)
        root.children.add(scriptsItem)

        list.isShowRoot = false
        list.root = root

        // init map
        this.mapGUI.init()
    }

    fun showSelectDialog(title: String, options: Collection<String>, callback: (String) -> Unit)
    {
        this.showSelectDialog(title, options.associateBy { it }, callback)
    }

    fun <T> showSelectDialog(title: String, options: Map<String, T>, callback: (T) -> Unit)
    {
        FXUtils.loadDialog(EditorApplication::class.java.classLoader, "select", title, this.scene.stage, Pair(options, callback))
    }

    fun showMapSelectDialog(title: String, callback: (ResourceView) -> Unit)
    {
        val choices: Map<String, ResourceView> = this.editor.bundle
                .getResourcesByCategory(MargoResource.Category.MAPS)
                .map { "${it.name} [${it.id}]" to it }
                .toMap()

        this.editor.gui.showSelectDialog(title, choices, callback)
    }

    fun reloadResourcesView(resources: Collection<ResourceView>)
    {
        val list = this.editor.gui.controller.indexList
        val scrollBar = list.lookup(".scroll-bar") as ScrollBar
        val previousScroll = scrollBar.value

        // pre-load icons
        val mapIcon = FXUtils.loadIcon("list/door.png")
        val itemIcon = FXUtils.loadIcon("list/item.png")
        val npcIcon = FXUtils.loadIcon("list/npc.png")
        val scriptIcon = FXUtils.loadIcon("list/script.png")

        // maps
        val maps = ArrayList<TreeItem<CustomText>>()
        val items = ArrayList<TreeItem<CustomText>>()
        val npcs = ArrayList<TreeItem<CustomText>>()
        val scripts = ArrayList<TreeItem<CustomText>>()

        val sortedResources = ArrayList(resources)

        Collections.sort(sortedResources) { o1, o2 ->
            val nameCompare = o1.name.compareTo(o2.name)

            if (nameCompare != 0)
            {
                nameCompare
            }
            else
            {
                o1.id.compareTo(o2.id)
            }
        }

        // load resources
        for (resource in sortedResources)
        {
            @Suppress("NON_EXHAUSTIVE_WHEN")
            when (resource.category)
            {
                MargoResource.Category.MAPS ->
                {
                    val contextMenu = ContextMenu()

                    val menuItemOpen = MenuItem("Otwórz mapę w edytorze")
                    menuItemOpen.setOnAction {
                        this.editor.mapEditor.selectMap(resource.id)
                    }
                    contextMenu.items.add(menuItemOpen)

                    val menuItemDelete = MenuItem("Usuń mapę z zestawu")
                    menuItemDelete.setOnAction {
                        this.editor.deleteResource(MargoResource.Category.MAPS, resource.id)
                    }
                    contextMenu.items.add(menuItemDelete)

                    maps.add(TreeItem(CustomText("${resource.name} [${resource.id}]", ImageView(mapIcon), contextMenu, menuItemOpen, menuItemDelete)))
                }
                MargoResource.Category.ITEMS ->
                {
                    val contextMenu = ContextMenu()

                    val menuItemOpen = MenuItem("Edytuj przedmiot")
                    menuItemOpen.setOnAction {
                        this.editor.itemEditor.edit(resource)
                    }
                    contextMenu.items.add(menuItemOpen)

                    val menuItemDelete = MenuItem("Usuń przedmiot z zestawu")
                    menuItemDelete.setOnAction {
                        this.editor.deleteResource(MargoResource.Category.ITEMS, resource.id)
                    }
                    contextMenu.items.add(menuItemDelete)

                    items.add(TreeItem(CustomText("${resource.name} [${resource.id}]", ImageView(itemIcon), contextMenu, menuItemOpen, menuItemDelete)))
                }
                MargoResource.Category.NPC ->
                {
                    val contextMenu = ContextMenu()

                    val menuItemOpen = MenuItem("Edytuj npc")
                    menuItemOpen.setOnAction {
                        this.editor.npcEditor.edit(resource)
                    }
                    contextMenu.items.add(menuItemOpen)

                    val menuItemDelete = MenuItem("Usuń npc z zestawu")
                    menuItemDelete.setOnAction {
                        this.editor.deleteResource(MargoResource.Category.NPC, resource.id)
                    }
                    contextMenu.items.add(menuItemDelete)

                    npcs.add(TreeItem(CustomText(resource.id, ImageView(npcIcon), contextMenu, menuItemOpen, menuItemDelete)))
                }
                MargoResource.Category.SCRIPTS ->
                {
                    val contextMenu = ContextMenu()

                    val menuItemOpen = MenuItem("Edytuj skrypt")
                    menuItemOpen.setOnAction {
                        this.editor.scriptEditor.edit(resource)
                    }
                    contextMenu.items.add(menuItemOpen)

                    val menuItemDelete = MenuItem("Usuń skrypt z zestawu")
                    menuItemDelete.setOnAction {
                        this.editor.deleteResource(MargoResource.Category.SCRIPTS, resource.id)
                    }
                    contextMenu.items.add(menuItemDelete)

                    scripts.add(TreeItem(CustomText(resource.id, ImageView(scriptIcon), contextMenu, menuItemOpen, menuItemDelete)))
                }
            }
        }

        mapsItem.children.setAll(maps)
        itemsItem.children.setAll(items)
        npcsItem.children.setAll(npcs)
        scriptsItem.children.setAll(scripts)

        scrollBar.value = previousScroll
    }

    data class CustomText(val text: String, val graphic: Node, val contextMenu: ContextMenu? = null, val defaultAction: MenuItem? = null, val deleteAction: MenuItem? = null)

    private class CustomCellFactory
        : Callback<TreeView<CustomText>, TreeCell<CustomText>>
    {
        override fun call(param: TreeView<CustomText>?): TreeCell<CustomText>
        {
            return CustomCell()
        }

        class CustomCell : TreeCell<CustomText>()
        {
            override fun updateItem(item: CustomText?, empty: Boolean)
            {
                super.updateItem(item, empty)

                if (item != null)
                {
                    this.text = item.text
                    this.graphic = item.graphic
                    this.contextMenu = item.contextMenu
                }
                else
                {
                    this.text = null
                    this.graphic = null
                    this.contextMenu = null
                }

                if (item?.defaultAction != null)
                {
                    this.setOnMouseClicked {
                        if (it.button == MouseButton.PRIMARY && it.clickCount == 2)
                        {
                            item.defaultAction.fire()
                        }
                    }
                }
                else
                {
                    this.onMouseClicked = null
                }
            }
        }
    }
}
/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Ordinastie
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package net.malisis.doors.gui;

import java.util.HashMap;
import java.util.Map.Entry;

import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.UISlot;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.container.UIPanel;
import net.malisis.core.client.gui.component.container.UIPlayerInventory;
import net.malisis.core.client.gui.component.container.UITabGroup;
import net.malisis.core.client.gui.component.container.UIWindow;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UICheckBox;
import net.malisis.core.client.gui.component.interaction.UISelect;
import net.malisis.core.client.gui.component.interaction.UISelect.Option;
import net.malisis.core.client.gui.component.interaction.UITab;
import net.malisis.core.client.gui.component.interaction.UITextField;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.malisis.core.inventory.MalisisInventoryContainer;
import net.malisis.core.util.TileEntityUtils;
import net.malisis.doors.door.DoorRegistry;
import net.malisis.doors.door.movement.IDoorMovement;
import net.malisis.doors.door.sound.IDoorSound;
import net.malisis.doors.entity.DoorFactoryTileEntity;
import net.malisis.doors.network.DoorFactoryMessage;

import com.google.common.eventbus.Subscribe;

/**
 * @author Ordinastie
 * 
 */
public class DoorFactoryGui extends MalisisGui
{
	private DoorFactoryTileEntity tileEntity;
	private UITab firstTab;
	private UISelect selDoorMovement;
	private UITextField tfOpenTime;
	private UICheckBox cbRedstone;
	private UICheckBox cbDoubleDoor;
	private UISelect selDoorSound;

	private static boolean firstTabActive = true;

	public DoorFactoryGui(DoorFactoryTileEntity te, MalisisInventoryContainer container)
	{
		setInventoryContainer(container);
		tileEntity = te;

		UIWindow window = new UIWindow("tile.door_factory.name", 190, 240);
		UIPanel propContainer = new UIPanel(180, 80);
		UIPanel matContainer = new UIPanel(180, 80);

		UITabGroup tabGroup = new UITabGroup().setPosition(0, 13);
		firstTab = tabGroup.addTab("gui.door_factory.tab_properties", propContainer).register(this);
		UITab tab2 = tabGroup.addTab("gui.door_factory.tab_materials", matContainer).register(this);

		tabGroup.setActiveTab(firstTabActive ? firstTab : tab2);

		setPropertiesContainer(propContainer);
		setMaterialsContainer(matContainer);

		UIButton btnCreate = new UIButton("gui.door_factory.create_door", 80).setPosition(50, 100).register(this);
		UISlot outputSlot = new UISlot(tileEntity.outputSlot).setPosition(81, 122);

		UIPlayerInventory playerInv = new UIPlayerInventory(container.getPlayerInventory());

		window.add(playerInv);

		window.add(tabGroup);
		window.add(propContainer);
		window.add(matContainer);

		window.add(btnCreate);
		window.add(outputSlot);

		addToScreen(window);

		TileEntityUtils.linkTileEntityToGui(tileEntity, this);
	}

	private void setPropertiesContainer(UIContainer propContainer)
	{
		HashMap<IDoorMovement, String> listMvt = new HashMap<>();
		for (Entry<String, IDoorMovement> entry : DoorRegistry.listMovements().entrySet())
			listMvt.put(entry.getValue(), entry.getKey());
		selDoorMovement = new UISelect(100, UISelect.Option.fromList(listMvt)).setPosition(0, 2, Anchor.RIGHT).register(this);
		selDoorMovement.setLabelPattern("door_movement.%s");

		tfOpenTime = new UITextField(30).setPosition(-5, 14, Anchor.RIGHT).setFilter("\\d+").register(this);
		cbRedstone = new UICheckBox().setPosition(-15, 26, Anchor.RIGHT).register(this);
		cbDoubleDoor = new UICheckBox().setPosition(-15, 38, Anchor.RIGHT).register(this);

		HashMap<IDoorSound, String> listSounds = new HashMap<>();
		for (Entry<String, IDoorSound> entry : DoorRegistry.listSounds().entrySet())
			listSounds.put(entry.getValue(), entry.getKey());
		selDoorSound = new UISelect(100, UISelect.Option.fromList(listSounds)).setPosition(0, 50, Anchor.RIGHT).register(this);
		selDoorSound.setLabelPattern("gui.door_factory.door_sound.%s");

		propContainer.add(new UILabel("gui.door_factory.door_movement").setPosition(0, 4));
		propContainer.add(new UILabel("gui.door_factory.door_open_time").setPosition(0, 16));
		propContainer.add(new UILabel("gui.door_factory.door_require_redstone").setPosition(0, 28));
		propContainer.add(new UILabel("gui.door_factory.door_double_door").setPosition(0, 40));
		propContainer.add(new UILabel("gui.door_factory.door_sound").setPosition(0, 52));

		propContainer.add(selDoorMovement);
		propContainer.add(tfOpenTime);
		propContainer.add(cbRedstone);
		propContainer.add(cbDoubleDoor);
		propContainer.add(selDoorSound);

	}

	private void setMaterialsContainer(UIContainer matContainer)
	{
		UISlot frameSlot = new UISlot(tileEntity.frameSlot).setPosition(-10, 4, Anchor.RIGHT);
		UISlot topMaterialSlot = new UISlot(tileEntity.topMaterialSlot).setPosition(-10, 24, Anchor.RIGHT);
		UISlot bottomMaterialSlot = new UISlot(tileEntity.bottomMaterialSlot).setPosition(-10, 44, Anchor.RIGHT);

		matContainer.add(new UILabel("gui.door_factory.frame_type").setPosition(0, 9));
		matContainer.add(new UILabel("gui.door_factory.top_material").setPosition(0, 29));
		matContainer.add(new UILabel("gui.door_factory.bottom_material").setPosition(0, 49));

		matContainer.add(frameSlot);
		matContainer.add(topMaterialSlot);
		matContainer.add(bottomMaterialSlot);
	}

	@Override
	public void updateGui()
	{
		selDoorMovement.setSelectedOption(tileEntity.getDoorMovement());
		tfOpenTime.setText(Integer.toString(tileEntity.getOpeningTime()));
		cbRedstone.setChecked(tileEntity.requireRedstone());
		cbDoubleDoor.setChecked(tileEntity.isDoubleDoor());
		selDoorSound.setSelectedOption(tileEntity.getDoorSound());
	}

	@Subscribe
	public void onGuiChangeEvent(ComponentEvent<UIComponent> event)
	{
		if (event instanceof ComponentEvent.StateChanged)
			return;

		Option opt = selDoorMovement.getSelectedOption();
		if (opt != null)
			tileEntity.setDoorMovement((IDoorMovement) opt.getKey());
		try
		{
			tileEntity.setOpeningTime(Integer.parseInt(tfOpenTime.getText()));
		}
		catch (NumberFormatException e)
		{
			tfOpenTime.setText(Integer.toString(tileEntity.getOpeningTime()));
		}
		tileEntity.setRequireRedstone(cbRedstone.isChecked());
		tileEntity.setDoubleDoor(cbDoubleDoor.isChecked());

		opt = selDoorSound.getSelectedOption();
		if (opt != null)
			tileEntity.setDoorSound((IDoorSound) opt.getKey());

		DoorFactoryMessage.sendDoorInformations(tileEntity);
	}

	@Subscribe
	public void onCreateDoor(UIButton.ClickedEvent event)
	{
		DoorFactoryMessage.sendCreateDoor(tileEntity);
	}

	@Subscribe
	public void onTabActivation(ComponentEvent.ActiveStateChanged<UITab> event)
	{
		if (event.getState())
			firstTabActive = event.getComponent() == firstTab;
	}

}

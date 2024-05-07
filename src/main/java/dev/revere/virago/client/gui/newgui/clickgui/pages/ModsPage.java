package dev.revere.virago.client.gui.newgui.clickgui.pages;

import dev.revere.virago.Virago;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.setting.Setting;
import dev.revere.virago.client.gui.newgui.IngameMenu;
import dev.revere.virago.client.gui.newgui.Page;
import dev.revere.virago.client.gui.newgui.clickgui.components.mods.*;
import dev.revere.virago.client.gui.newgui.framework.Menu;
import dev.revere.virago.client.gui.newgui.framework.TextPattern;
import dev.revere.virago.client.gui.newgui.framework.components.MenuButton;
import dev.revere.virago.client.gui.newgui.framework.components.MenuTextField;
import dev.revere.virago.client.services.FontService;
import dev.revere.virago.client.services.ModuleService;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Athena Development
 * @project Athena-Client
 * @date 6/2/2023
 */

public class ModsPage extends Page {
	public final int MENU_HEADER_TEXT_COLOR_MOD = new Color(129, 129, 129, IngameMenu.MENU_ALPHA).getRGB();

	public final static int MENU_BG_COLOR_MOD = new Color(30, 30, 30, IngameMenu.MENU_ALPHA).getRGB();
	public final int MENU_BG_COLOR_MOD_BORDER = new Color(30, 30, 30, IngameMenu.MENU_ALPHA).getRGB();
	public final static int MENU_SIDE_BG_COLOR = new Color(30, 30, 30, IngameMenu.MENU_ALPHA).getRGB();

	private final ResourceLocation[] MOD_TABS = new ResourceLocation[EnumModuleType.values().length];

	private final FontService fontService = Virago.getInstance().getServiceManager().getService(FontService.class);
	private EnumModuleType modCategory = EnumModuleType.COMBAT;
	public AbstractModule activeModule;
	private String search;

	public ModsPage(Minecraft mc, Menu menu, IngameMenu parent) {
		super(mc, menu, parent);
	}

	@Override
	public void onInit() { }

	@Override
	public void onRender() {
		int y = menu.getY() + 59;
		int height = 32;
		GlStateManager.color(1,1,1);

		drawVerticalLine(menu.getX() + 215, y + height - 30, height + 432, 3, new Color(0,0,0,0).getRGB());

		y += 50;

		for (EnumModuleType category : EnumModuleType.values()) {
			y += height + 2 + 10;
		}

		y = menu.getY() + menu.getHeight() - height;
		//drawShadowUp(menu.getX(), y - 10, 215);

		if (modCategory != null) {
			fontService.getProductSans28().drawString(activeModule != null ? activeModule.getName().toUpperCase() : modCategory.getName(), menu.getX() + 255, menu.getY() + 20, -1);
			fontService.getProductSans().drawString("Configure build-in client mods", menu.getX() + 255, menu.getY() + 35, -1);
		}
	}

	@Override
	public void onLoad() {
		int y = 59 + 15;
		int x = 255;
		int height = 32;

		for (EnumModuleType category : EnumModuleType.values()) {
			MenuButton comp = new ModCategoryButton(category, x - 20, y, (int) (35 + fontService.getProductSans28().getStringWidth(category.getName())), height) {
				@Override
				public void onAction() {
					for (dev.revere.virago.client.gui.newgui.framework.MenuComponent component : menu.getComponents()) {
						if (component instanceof ModCategoryButton) {
							ModCategoryButton button = (ModCategoryButton) component;
							button.setActive(component == this);
						}
					}

					modCategory = category;
					activeModule = null;
					ModsPage.this.parent.initPage();
				}
			};

			if (category == modCategory) {
				comp.setActive(true);
			}

			menu.addComponent(comp);
			x += fontService.getProductSans28().getStringWidth(category.getName()) + 20;
			//y += height + 2 + 10;
		}

		if (activeModule == null) {
			MenuTextField searchbar = new SearchTextfield(TextPattern.NONE, menu.getWidth() - 31 - 250 - 5, 110 - 38, 250, 30) {
				@Override
				public void onAction() {
					search = getText();
					initModPage();
				}

				@Override
				public void onClick() {
					setText("");
					search = "";
					initModPage();
				}
			};

			searchbar.setText(search != null ? search : "");
			menu.addComponent(searchbar);
		} else {
			int w = 150;
			int h = 20;

			ModsButton enable = new ModsButton(activeModule.isEnabled() ? "DISABLE" : "ENABLE", 255, menu.getHeight() - h - 6) {
				@Override
				public void onAction() {
					activeModule.setEnabled(isActive());

					setText(activeModule.isEnabled() ? "DISABLE" : "ENABLE");
				}
			};

			enable.setActive(activeModule.isEnabled());

			menu.addComponent(enable);

			/*MenuModList list = new MenuModList(BindType.class, menu.getWidth() - 182 - 160, menu.getHeight() - h - 6, 20) {
				@Override
				public void onAction() {
					activeModule.setBindType(BindType.valueOf(getValue().toUpperCase()));
				}
			};

			list.setValue(activeModule.getBindType().toString());

			menu.addComponent(list);*/

			MenuModKeybind btn = new MenuModKeybind(menu.getWidth() - 182, menu.getHeight() - h - 6, w, h) {
				@Override
				public void onAction() {
					activeModule.setKey(getBind());
					setX(menu.getWidth() - 182);
				}
			};

			btn.setBind(activeModule.getKey());

			menu.addComponent(btn);

			menu.addComponent(new GoBackButton(menu.getWidth() - 154, 110 - 38) {
				@Override
				public void onAction() {
					activeModule = null;
					ModsPage.this.parent.initPage();
				}
			});
		}

		ModScrollPane pane = new ModScrollPane(255, 140, menu.getWidth() - 255 - 32, menu.getHeight() - 141, false);
		menu.addComponent(pane);

		if (activeModule == null) {
			initModPage(pane);
		} else {
			pane.setX(225 + 1);
			pane.setY(110 + 5 + 1);
			pane.setWidth(menu.getWidth() - 255 - 33);
			pane.setHeight(menu.getHeight() - 110 - 52 - 5);

			pane.setFullHeightScroller(true);

			pane.getComponents().clear();

			List<dev.revere.virago.client.gui.newgui.framework.MenuComponent> toAdd = new ArrayList<>();

			int xSpacing = 25;
			int ySpacing = 15;

			int sliderWidth = pane.getWidth() - xSpacing * 2;

			for (Setting setting : activeModule.getSettings()) {
				GlStateManager.color(1,1,1);
				final FeatureText label;

				String key = setting.getName();

				if (setting.getDescription() == null) {
					toAdd.add(label = new FeatureText(key, 0, 0));
				} else {
					toAdd.add(label = new FeatureText(key, setting.getDescription(), 0, 0));
				}

				if (setting.getValue() instanceof Boolean) {
					MenuModCheckbox checkbox = new MenuModCheckbox(0, 0, 30, 15) {
						@Override
						public void onAction() {
							((Setting<Boolean>) setting).setValue(isChecked());
						}
					};

					checkbox.setChecked(((Setting<Boolean>) setting).getValue());
					toAdd.add(checkbox);
				} else if (setting.getValue() instanceof Color) {
					Color entry = (Color) setting.getValue();
					toAdd.add(new MenuModNewColorPicker(0, 0, 35, 90, entry.getRGB()) {
						@Override
						public void onAction() {
							((Setting<Color>) setting).setValue(getColor());
						}
					});
				} else if (setting.getValue() instanceof Double) {
					FeatureValueText valueText = new FeatureValueText("", 0, 0);
					toAdd.add(valueText);

					double entry = (double) setting.getValue();
					double minimum = (double) setting.getMinimum();
					double maximum = (double) setting.getMaximum();

					MenuModSlider slider = new MenuModSlider(entry, minimum, maximum, 2, 0, 0, sliderWidth, 15) {
						@Override
						public void onAction() {
							((Setting<Double>) setting).setValue((double) getValue());
							valueText.setText(getValue() + "");
						}
					};

					slider.onAction();
					toAdd.add(slider);
				} else if (setting.getValue() instanceof Float) {
					FeatureValueText valueText = new FeatureValueText("", 0, 0);
					toAdd.add(valueText);

					float entry = (float) setting.getValue();
					float minimum = (float) setting.getMinimum();
					float maximum = (float) setting.getMaximum();

					MenuModSlider slider = new MenuModSlider(entry, minimum, maximum, 2, 0, 0, sliderWidth, 15) {
						@Override
						public void onAction() {
							label.setText((setting.getName() + " | ").toUpperCase());
							((Setting<Float>) setting).setValue(getValue());

							valueText.setText(getValue() + "");
						}
					};

					slider.onAction();

					toAdd.add(slider);
				} else if (setting.getValue() instanceof Integer) {
					FeatureValueText valueText = new FeatureValueText("", 0, 0);
					int entry = (int) setting.getValue();
					int minimum = (int) setting.getMinimum();
					int maximum = (int) setting.getMaximum();

					toAdd.add(valueText);
						MenuModSlider slider = new MenuModSlider(entry, minimum, maximum, 0, 0, sliderWidth, 15) {
							@Override
							public void onAction() {
								label.setText((setting.getName() + " | ").toUpperCase());
								((Setting<Integer>) setting).setValue(getIntValue());

								valueText.setText(getIntValue() + "");
							}
						};

						slider.onAction();
						toAdd.add(slider);
				} else if (setting.getValue() instanceof String) {
					String entry = (String) setting.getValue();
					ModTextbox box = new ModTextbox(TextPattern.NONE, 0, 0, 175, 15) {
						@Override
						public void onAction() {
							((Setting<String>) setting).setValue(getText());
						}
					};

					box.setText(entry);
					toAdd.add(box);
				}
			}

			int defaultX = 25;

			int xPos = defaultX;
			int yPos = 25;

			boolean isText = false;
			dev.revere.virago.client.gui.newgui.framework.MenuComponent last = null;

			for (dev.revere.virago.client.gui.newgui.framework.MenuComponent component : toAdd) {
				if (component instanceof FeatureValueText) {
					if (last != null) {
						component.setX(xPos);
						component.setY(yPos);
					}
				} else if (component instanceof FeatureText) {
					component.setX(xPos);
					component.setY(yPos);

					xPos += component.getWidth();

					isText = true;
				} else {
					xPos = defaultX;

					if (isText) {
						if (component instanceof MenuModSlider) {
							yPos += ySpacing;

							component.setX(xPos);
							component.setY(yPos);
						} else {
							if (component instanceof MenuModList) {
								component.setX(pane.getWidth() - component.getWidth() - xSpacing * 3 + 12);
							} else {
								component.setX(pane.getWidth() - component.getWidth() - xSpacing);
							}

							component.setY(yPos);
						}

						isText = false;
					} else {
						component.setX(xPos);
						component.setY(yPos);
					}

					yPos += ySpacing + component.getHeight();
				}

				pane.addComponent(component);

				last = component;
			}
		}
	}

	@Override
	public void onUnload() {
		activeModule = null;
		ModsPage.this.parent.initPage();
	}

	@Override
	public void onOpen() {
		updateStates();
	}

	@Override
	public void onClose() {

	}

	private void updateStates() {
		for (dev.revere.virago.client.gui.newgui.framework.MenuComponent component : menu.getComponents()) {
			if (component instanceof ModsButton) {
				ModsButton button = (ModsButton) component;

				button.setActive(activeModule.isEnabled());
				button.onAction();

				break;
			}
		}
	}

	private void initModPage() {
		for (dev.revere.virago.client.gui.newgui.framework.MenuComponent component : menu.getComponents()) {
			if (component instanceof ModScrollPane) {
				initModPage((ModScrollPane) component);
				return;
			}
		}
	}

	private void initModPage(ModScrollPane pane) {
		pane.getComponents().clear();

		for (dev.revere.virago.client.gui.newgui.framework.MenuComponent c : menu.getComponents()) {
			if (c instanceof ModsButton || c instanceof MenuModList) {
				menu.getComponents().remove(c);
			}
		}

		int x = 0;
		int y = 5;
		int width = 170;
		int height = 150;
		int spacing = 16;

		List<AbstractModule> modules = Virago.getInstance().getServiceManager().getService(ModuleService.class).getModuleList().stream()
				.filter(entry ->
						((search == null || search.isEmpty()) || entry.getName().toLowerCase().contains(search.toLowerCase())) &&
								(entry.getType() == modCategory))
				.collect(Collectors.toList());

		modules.sort(Comparator.comparing(module -> module.getName().toLowerCase()));

		for (AbstractModule module : modules) {
			pane.addComponent(new ModuleBox(module, x, y, width, height) {
				@Override
				public void onOpenSettings() {
					activeModule = module;
					ModsPage.this.parent.initPage();
				}
			});

			x += width + spacing;

			if (x + width >= pane.getWidth()) {
				x = 0;
				y += height + spacing;
			}
		}
	}
}
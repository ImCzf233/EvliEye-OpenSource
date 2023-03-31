/*

 *



 */
package gg.evlieye.settings;

import java.awt.Color;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import gg.evlieye.EvlieyeClient;
import gg.evlieye.clickgui.Component;
import gg.evlieye.clickgui.components.ColorComponent;
import gg.evlieye.keybinds.PossibleKeybind;
import gg.evlieye.util.ColorUtils;
import gg.evlieye.util.json.JsonException;
import gg.evlieye.util.json.JsonUtils;

public final class ColorSetting extends Setting
{
	private Color color;
	private final Color defaultColor;
	
	public ColorSetting(String name, String description, Color color)
	{
		super(name, description);
		this.color = Objects.requireNonNull(color);
		defaultColor = color;
	}
	
	public ColorSetting(String name, Color color)
	{
		this(name, "", color);
	}
	
	public Color getColor()
	{
		return color;
	}
	
	public float[] getColorF()
	{
		float red = color.getRed() / 255F;
		float green = color.getGreen() / 255F;
		float blue = color.getBlue() / 255F;
		return new float[]{red, green, blue};
	}
	
	public int getColorI()
	{
		return color.getRGB();
	}
	
	public int getRed()
	{
		return color.getRed();
	}
	
	public int getGreen()
	{
		return color.getGreen();
	}
	
	public int getBlue()
	{
		return color.getBlue();
	}
	
	public Color getDefaultColor()
	{
		return defaultColor;
	}
	
	public void setColor(Color color)
	{
		this.color = Objects.requireNonNull(color);
		EvlieyeClient.INSTANCE.saveSettings();
	}
	
	@Override
	public Component getComponent()
	{
		return new ColorComponent(this);
	}
	
	@Override
	public void fromJson(JsonElement json)
	{
		if(!JsonUtils.isString(json))
			return;
		
		try
		{
			setColor(ColorUtils.parseHex(json.getAsString()));
			
		}catch(JsonException e)
		{
			e.printStackTrace();
			setColor(defaultColor);
		}
	}
	
	@Override
	public JsonElement toJson()
	{
		return new JsonPrimitive(ColorUtils.toHex(color));
	}
	
	@Override
	public Set<PossibleKeybind> getPossibleKeybinds(String featureName)
	{
		String description = "Set " + featureName + " " + getName() + " to ";
		String command = ".setcolor " + featureName.toLowerCase() + " "
			+ getName().toLowerCase().replace(" ", "_") + " ";
		
		LinkedHashSet<PossibleKeybind> pkb = new LinkedHashSet<>();
		addPKB(pkb, command + "#FF0000", description + "red");
		addPKB(pkb, command + "#00FF00", description + "green");
		addPKB(pkb, command + "#0000FF", description + "blue");
		addPKB(pkb, command + "#FFFF00", description + "yellow");
		addPKB(pkb, command + "#00FFFF", description + "cyan");
		addPKB(pkb, command + "#FF00FF", description + "magenta");
		addPKB(pkb, command + "#FFFFFF", description + "white");
		addPKB(pkb, command + "#000000", description + "black");
		return pkb;
	}
	
	private void addPKB(LinkedHashSet<PossibleKeybind> pkb, String command,
		String description)
	{
		pkb.add(new PossibleKeybind(command, description));
	}
}
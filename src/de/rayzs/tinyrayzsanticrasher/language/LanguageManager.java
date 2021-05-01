package de.rayzs.tinyrayzsanticrasher.language;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;

import de.rayzs.tinyrayzsanticrasher.enums.LanguageEnum;
import de.rayzs.tinyrayzsanticrasher.enums.MessageEnum;
import de.rayzs.tinyrayzsanticrasher.file.FileManager;
import de.rayzs.tinyrayzsanticrasher.plugin.TinyRayzsAntiCrasher;

public class LanguageManager {
	
	final protected String filePath = "plugins/TinyRayzsAntiCrasher";
	private HashMap<MessageEnum, String> languageHash_DE, languageHash_EN, languageHash_PL, languageHash_TR;
	private FileManager languageFile;
	
	public LanguageManager(final TinyRayzsAntiCrasher instance) {
		languageHash_DE = new HashMap<>();
		languageHash_EN = new HashMap<>();
		languageHash_PL = new HashMap<>();
		languageHash_TR = new HashMap<>();
		languageFile = new FileManager(instance, new File(filePath, "language.yml"));
		final List<MessageEnum> messageEnumList = Arrays.<MessageEnum>asList(MessageEnum.class.getEnumConstants());
		messageEnumList.forEach(this::loadMessages);
	}
	
	protected void loadMessages(final MessageEnum messageEnum) {
        switch (messageEnum) {
        case DETECTED_KICK_MESSAGE:
        	setMessage(messageEnum
        			, "&8[&bT&9R&bA&9C&8] &e%PLAYER% &7hat versucht zu crashen&8!"
        			, "&8[&bT&9R&bA&9C&8] &e%PLAYER% &7tried to crash&8!"
        			, "&8[&bT&9R&bA&9C&8] &e%PLAYER% &7próbował się zawiesić&8!"
        			, "&8[&bT&9R&bA&9C&8] &e%PLAYER% &7çökmeyi denedi&8!");
        	break;
		case DETECTED_KICK_REASON:
			setMessage(messageEnum
        			, "&8[&bT&9R&bA&9C&8] &cVersuch es erst gar nicht!"
        			, "&8[&bT&9R&bA&9C&8] &cDo not even try it!"
        			, "&8[&bT&9R&bA&9C&8] &cNawet tego nie próbuj!"
        			, "&8[&bT&9R&bA&9C&8] &cDeneme bile!");
			break;
		default:
			break;
        }
	}
	
	public String getMessage(final LanguageEnum languageEnum, final MessageEnum messageEnum) {
		String message = "";
		switch(languageEnum) {
		case DE:
			message = languageHash_DE.get(messageEnum);
			break;
		case EN:
			message = languageHash_EN.get(messageEnum);
			break;
		case PL:
			message = languageHash_PL.get(messageEnum);
			break;
		case TR:
			message = languageHash_TR.get(messageEnum);
			break;
		default:
			message = languageHash_EN.get(messageEnum);
			break;
		}
		return ChatColor.translateAlternateColorCodes('&', message);
	}
	
	protected void setMessage(final MessageEnum messageEnum, final String de, final String en, final String pl, final String tr) {
		saveMessage(LanguageEnum.DE, messageEnum, de);
		saveMessage(LanguageEnum.EN, messageEnum, en);
    	saveMessage(LanguageEnum.PL, messageEnum, pl);
    	saveMessage(LanguageEnum.TR, messageEnum, tr);
	}
	
	protected void saveMessage(final LanguageEnum languageEnum, final MessageEnum messageEnum, final String message) {
		switch(languageEnum) {
		case DE:
			languageHash_DE.put(messageEnum, languageFile.search(languageEnum.toString().toLowerCase() + "." + messageEnum.toString().toLowerCase()).getString(message));
			break;
		case EN:
			languageHash_EN.put(messageEnum, languageFile.search(languageEnum.toString().toLowerCase() + "." + messageEnum.toString().toLowerCase()).getString(message));
			break;
		case PL:
			languageHash_PL.put(messageEnum, languageFile.search(languageEnum.toString().toLowerCase() + "." + messageEnum.toString().toLowerCase()).getString(message));
			break;
		case TR:
			languageHash_TR.put(messageEnum, languageFile.search(languageEnum.toString().toLowerCase() + "." + messageEnum.toString().toLowerCase()).getString(message));
			break;
		}
	}
}
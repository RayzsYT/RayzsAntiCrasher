package de.rayzs.rayzsanticrasher.crasher.meth;

import de.rayzs.rayzsanticrasher.actionbar.Actionbar;

public class LiveAttackCounter {

	private Attack attack;

	public LiveAttackCounter(Attack attack, Integer time) {
		this.attack = attack;
		(new Thread(() -> {
			while (attack.isUnderAttack())
				try {
					Thread.sleep(time);
					send();
				} catch (InterruptedException error) {
				}

		})).start();
	}

	private void send() {
		String text;
		Integer blacklistSize = attack.getBlacklistSize();
		Integer totalConnectionsPerSecond = attack.getConnections();
		text = "§8» §8[§4§n" + attack.getTaskName()
				+ "§8] §c§nSERVER IS UNDER ATTACK§8! §7Blocked§8-§7IP§8'§7s§8: §b§l§o§n" + blacklistSize
				+ "§8 | §7CPS§8: §b" + totalConnectionsPerSecond + " §8«";
		new Actionbar(text, "rayzsanticrasher.attack");
	}

}

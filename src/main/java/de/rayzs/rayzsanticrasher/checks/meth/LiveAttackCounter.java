package de.rayzs.rayzsanticrasher.checks.meth;

import de.rayzs.rayzsanticrasher.actionbar.Actionbar;
import de.rayzs.rayzsanticrasher.plugin.RayzsAntiCrasher;

public class LiveAttackCounter {

	private RayzsAntiCrasher instance;
	private Attack attack;

	public LiveAttackCounter(Attack attack, Integer time) {
		instance = RayzsAntiCrasher.getInstance();
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
		text = instance.getLiveAttackMessage(attack);
		new Actionbar(text, "rayzsanticrasher.attack");
	}
}
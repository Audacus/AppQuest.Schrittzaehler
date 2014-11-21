// http://stackoverflow.com/questions/604424/java-convert-string-to-enum
package ch.fenceposts.appquest.schrittzaehler.direction;

public enum Direction {
	RIGHT("rechts"),
	LEFT("links");
	
	private String direction;
	
	Direction(String direction) {
		this.direction = direction;
	}
	
	public String getDirection() {
		return this.direction;
	}
	
	public static Direction fromString(String direction) {
		if (direction != null) {
			for (Direction d : Direction.values()) {
				if (direction.equalsIgnoreCase(d.direction)) {
					return d;
				}
			}
		}
		throw new IllegalArgumentException("No constant with direction " + direction + " found");
	}
}

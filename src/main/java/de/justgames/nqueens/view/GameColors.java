package de.justgames.nqueens.view;

import javafx.scene.paint.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class GameColors {
    private static final Map<Integer, Color> colorMap = new HashMap<>();
    private static final double GOLDEN_RATIO_CONJUGATE = 0.618033988749895;
    private static double currHue = new Random().nextDouble();

    public static Color getColorForRegion(int regionId) {
        if (colorMap.containsKey(regionId)) {
            return colorMap.get(regionId);
        }

        currHue += GOLDEN_RATIO_CONJUGATE;
        currHue %= 1.0;
        double saturation = 0.4;
        double brightness = 0.95; 
        Color color = Color.hsb(currHue * 360, saturation, brightness);
        colorMap.put(regionId, color);
        
        return color;
    }
}
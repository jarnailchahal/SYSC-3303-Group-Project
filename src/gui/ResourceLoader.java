package gui;

import common.Config;
import common.Log;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 * 
 */
public class ResourceLoader {
    /** */
    private static Map<ResourceType, BufferedImage> resources;

    static {
        resources = new HashMap<>();
        for (ResourceType type : ResourceType.values()) {
            String name = type.name().toLowerCase() + ".png";
            String path = Paths.get(Config.TEXTURES).resolve(name).toString();
            try {
                resources.put(type, ImageIO.read(new File(path)));
            } catch (IOException e) {
                Log.print("Failed to load: " + path);
                resources.put(type, new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB));
            }
        }
    }

    /**
     * 
     * @param type
     * @return
     */
    public static BufferedImage getResource(ResourceType type) {
        return resources.get(type);
    }

    /**
     * 
     * @param type
     * @return
     */
    public static int getWidth(ResourceType type) {
        return getResource(type).getWidth();
    }

    /**
     * 
     * @param type
     * @return
     */
    public static int getHeight(ResourceType type) {
        return getResource(type).getHeight();
    }
}

package colosseum.construction;

import colosseum.construction.manager.ConstructionSiteManager;
import colosseum.construction.manager.ManagerDependency;
import colosseum.utility.UtilZipper;
import colosseum.utility.WorldMapConstants;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.Validate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.ServiceLoader;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PluginUtils {

    @SuppressWarnings("ConstantConditions")
    private static void discoverManagers_TopologicalSort(
            Class<? extends ConstructionSiteManager> currentReference,
            Set<Class<? extends ConstructionSiteManager>> visited,
            Set<Class<? extends ConstructionSiteManager>> visiting,
            List<Class<? extends ConstructionSiteManager>> res
    ) {
        if (visited.contains(currentReference)) {
            return;
        }
        if (!visiting.add(currentReference)) {
            throw new IllegalStateException("Bro, you can't be this terrible having composed a cycling directed graph. Please double check " + currentReference.getName());
        }
        ManagerDependency anno = currentReference.getAnnotation(ManagerDependency.class);
        if (anno != null) {
            for (Class<? extends ConstructionSiteManager> aClass : anno.value()) {
                discoverManagers_TopologicalSort(aClass, visited, visiting, res);
            }
        }
        visiting.remove(currentReference);
        visited.add(currentReference);
        res.add(currentReference);
    }

    public static List<Class<? extends ConstructionSiteManager>> discoverManagers(List<Class<? extends ConstructionSiteManager>> discovered) {
        Queue<Class<? extends ConstructionSiteManager>> pending = new ArrayDeque<>(discovered);
        Set<Class<? extends ConstructionSiteManager>> topo_visited = new HashSet<>();
        Set<Class<? extends ConstructionSiteManager>> topo_visiting = new HashSet<>();
        List<Class<? extends ConstructionSiteManager>> res = new ArrayList<>();

        while (!pending.isEmpty()) {
            discoverManagers_TopologicalSort(pending.poll(), topo_visited, topo_visiting, res);
        }
        return res;
    }

    public static List<Class<? extends ConstructionSiteManager>> discoverManagers() {
        List<Class<? extends ConstructionSiteManager>> discovered = new ArrayList<>();
        ServiceLoader<ConstructionSiteManager> loader = ServiceLoader.load(ConstructionSiteManager.class, ConstructionSiteManager.class.getClassLoader());
        for (ConstructionSiteManager manager : loader) {
            discovered.add(manager.getClass());
        }
        return discoverManagers(discovered);
    }

    public static void registerManagers(
            List<Class<? extends ConstructionSiteManager>> managersReference,
            Map<Class<? extends ConstructionSiteManager>, ConstructionSiteManager> managers
    ) {
        try {
            for (Class<? extends ConstructionSiteManager> aClass : managersReference) {
                ConstructionSiteManager manager = aClass.getDeclaredConstructor().newInstance();
                managers.put(aClass, manager);
                ConstructionSiteProvider.getSite().getPluginLogger().info("Registering " + manager.getName());
                manager.register();
            }
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    public static void unregisterManagers(
            List<Class<? extends ConstructionSiteManager>> managersReference,
            Map<Class<? extends ConstructionSiteManager>, ConstructionSiteManager> managers
    ) {
        for (int i = managersReference.size() - 1; i >= 0; i--) {
            managers.computeIfPresent(managersReference.get(i), (c, in) -> {
                ConstructionSiteProvider.getSite().getPluginLogger().info("Unregistering " + in.getName());
                in.unregister();
                return null;
            });
        }
    }

    public static void unzip() {
        ConstructionSite site = ConstructionSiteProvider.getSite();
        File destination = site.getWorldContainer().toPath().resolve(WorldMapConstants.WORLD).toFile();
        if (destination.exists() && !destination.isDirectory()) {
            try {
                FileUtils.delete(destination);
            } catch (Exception e) {
                throw new Error(e);
            }
        }
        Validate.isTrue(BaseUtils.initDir(destination), "Cannot initialize directory");
        if (destination.listFiles().length == 0) {
            File zip = new File(destination, "Lobby.zip");
            try (InputStream inputStream = site.getClass().getClassLoader().getResourceAsStream("Lobby.zip");
                 FileOutputStream out = new FileOutputStream(zip)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                out.close();
                inputStream.close();
                Validate.isTrue(Files.asByteSource(zip).hash(Hashing.sha256()).toString().equals("73bb1f64abef831d84d2832c8b88fe87a1785dc2bfe30b6de6b592706a4cbaa1"), "Wrong file.");
                UtilZipper.unzip(zip, destination);
            } catch (Exception e) {
                throw new Error(e);
            } finally {
                FileUtils.deleteQuietly(zip);
            }
        }
    }

    /**
     * @param dataFolder Plugin data folder
     * @param filename Single filename
     * @return File object
     */
    public static File loadYml(File dataFolder, String filename) {
        File file = new File(dataFolder, filename);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Throwable t) {
                throw new Error(t);
            }
        }
        return file;
    }
}

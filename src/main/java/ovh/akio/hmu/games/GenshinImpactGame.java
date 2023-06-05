package ovh.akio.hmu.games;

import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;
import ovh.akio.hmu.Utils;
import ovh.akio.hmu.entities.PckAudioFile;
import ovh.akio.hmu.exceptions.InvalidGameDirectoryException;
import ovh.akio.hmu.games.abstracts.DifferentialPatchingGame;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GenshinImpactGame extends DifferentialPatchingGame {

    private final File               basePath;
    private final File               updatePackage;
    private       List<PckAudioFile> gamePckAudioFiles = null;

    public GenshinImpactGame(File basePath) {

        this.basePath = basePath;
        File gameDirectory = this.getGameDirectory();

        File gameExecutable = new File(gameDirectory, "GenshinImpact.exe");

        if (!gameExecutable.exists()) {
            throw new InvalidGameDirectoryException("The path provided does not point to a valid game directory.");
        }

        List<File> gameFiles = Utils.getDirectoryContent(this.getGameDirectory());
        Pattern    pattern   = Pattern.compile("game_(?<in>.*)_(?<out>.*)_hdiff_(?<sign>.*)\\.zip");

        this.updatePackage = gameFiles.stream()
                                      .filter(file -> {
                                          Matcher matcher = pattern.matcher(file.getName());
                                          return matcher.matches();
                                      })
                                      .findFirst().orElse(null);
    }

    @Override
    public boolean mayHandleArchiveItem(ISimpleInArchiveItem archiveItem) throws SevenZipException {
        boolean isPckDiff = archiveItem.getPath().endsWith(".pck.hdiff");
        boolean isMinimum = archiveItem.getPath().contains("Minimum");
        boolean isMusic   = archiveItem.getPath().contains("Music");

        return isPckDiff && (isMinimum || isMusic);
    }

    @Override
    public File getGameDirectory() {

        return new File(this.basePath, "Genshin Impact game");
    }

    @Override
    public File getAudioDirectory() {

        return new File(this.getGameDirectory(), "GenshinImpact_Data\\StreamingAssets\\AudioAssets");
    }

    @Override
    public String getName() {

        return "Genshin Impact";
    }

    @Override
    public String getShortName() {

        return "gi";
    }

    @Override
    public List<PckAudioFile> getAudioFiles() {

        if (this.gamePckAudioFiles != null) {
            return this.gamePckAudioFiles;
        }

        this.gamePckAudioFiles = Utils.scanPck(this.getAudioDirectory());
        return this.gamePckAudioFiles;
    }

    /**
     * Retrieve an {@link Optional} {@link File} representing the update package.
     *
     * @return An {@link Optional} {@link File}.
     */
    @Override
    public Optional<File> getUpdatePackageFile() {

        return Optional.ofNullable(this.updatePackage);
    }
}

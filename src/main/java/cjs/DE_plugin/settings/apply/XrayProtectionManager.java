package cjs.DE_plugin.settings.apply;

import cjs.DE_plugin.DE_plugin;
import cjs.DE_plugin.settings.SettingsManager;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ban.xray 설정을 서버의 Paper Anti-Xray 엔진(config/paper-world-defaults.yml,
 * config/paper-<world>.yml)과 동기화합니다. Bukkit/Paper 플러그인 API는 청크 패킷을
 * 직접 다루지 않으므로, 실제 블록 투시 차단은 Paper 자체의 Anti-Xray 기능에 위임하고
 * 이 클래스는 해당 설정 파일의 anticheat.anti-xray.enabled 값만 갱신합니다.
 * Paper는 이 값을 월드 로드 시점에 읽으므로, 완전히 적용되려면 서버 재시작이 필요합니다.
 */
public class XrayProtectionManager {

    private final DE_plugin plugin;
    private final SettingsManager sm;

    private static final Pattern ENABLED_LINE = Pattern.compile("^(\\s*)enabled:\\s*(true|false)(.*)$");

    public XrayProtectionManager(DE_plugin plugin) {
        this.plugin = plugin;
        this.sm = plugin.getSettingsManager();
    }

    public void applyXraySetting() {
        boolean enableAntiXray = sm.getBoolean(SettingsManager.XRAY_BANNED);

        Path configDir = Bukkit.getWorldContainer().toPath().resolve("config");
        if (!Files.isDirectory(configDir)) {
            plugin.getLogger().warning("config 폴더를 찾을 수 없어 Anti-Xray 설정을 동기화하지 못했습니다: " + configDir);
            return;
        }

        boolean patchedAny = false;
        List<Path> targetFiles;
        try (var stream = Files.list(configDir)) {
            targetFiles = stream
                    .filter(path -> {
                        String name = path.getFileName().toString();
                        return name.equals("paper-world-defaults.yml") ||
                                (name.startsWith("paper-") && name.endsWith(".yml"));
                    })
                    .toList();
        } catch (IOException e) {
            plugin.getLogger().warning("Anti-Xray 설정 파일 목록을 읽는 중 오류가 발생했습니다: " + e.getMessage());
            return;
        }

        for (Path file : targetFiles) {
            if (patchAntiXrayFile(file, enableAntiXray)) {
                patchedAny = true;
            }
        }

        if (patchedAny) {
            plugin.getLogger().info("Anti-Xray 설정을 " + (enableAntiXray ? "활성화" : "비활성화") +
                    " 상태로 동기화했습니다. 완전히 적용하려면 서버를 재시작하세요.");
        }
    }

    /**
     * 파일에서 "anticheat: -> anti-xray: -> enabled:" 경로를 찾아 값을 갱신합니다.
     * 구조를 찾지 못하거나 이미 원하는 값이면 아무것도 바꾸지 않고 false를 반환합니다.
     * 주석/다른 설정은 건드리지 않고 enabled 한 줄만 수정합니다.
     */
    private boolean patchAntiXrayFile(Path file, boolean enabled) {
        List<String> lines;
        try {
            lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            plugin.getLogger().warning(file.getFileName() + " 파일을 읽지 못했습니다: " + e.getMessage());
            return false;
        }

        Integer anticheatIndent = null;
        Integer antiXrayIndent = null;
        int targetLine = -1;
        String replacement = null;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }
            int indent = indentOf(line);

            if (antiXrayIndent != null && indent <= antiXrayIndent) {
                antiXrayIndent = null;
            }
            if (anticheatIndent != null && indent <= anticheatIndent) {
                anticheatIndent = null;
                antiXrayIndent = null;
            }

            if (anticheatIndent == null && trimmed.equals("anticheat:")) {
                anticheatIndent = indent;
                continue;
            }

            if (anticheatIndent != null && antiXrayIndent == null && trimmed.equals("anti-xray:")) {
                antiXrayIndent = indent;
                continue;
            }

            if (antiXrayIndent != null && trimmed.startsWith("enabled:")) {
                Matcher m = ENABLED_LINE.matcher(line);
                if (m.matches()) {
                    if (!m.group(2).equals(String.valueOf(enabled))) {
                        targetLine = i;
                        replacement = m.group(1) + "enabled: " + enabled + m.group(3);
                    }
                }
                break;
            }
        }

        if (targetLine == -1) {
            return false;
        }

        lines.set(targetLine, replacement);
        try {
            Files.write(file, lines, StandardCharsets.UTF_8);
            return true;
        } catch (IOException e) {
            plugin.getLogger().warning(file.getFileName() + " 파일에 쓰지 못했습니다: " + e.getMessage());
            return false;
        }
    }

    private int indentOf(String line) {
        int i = 0;
        while (i < line.length() && line.charAt(i) == ' ') i++;
        return i;
    }
}

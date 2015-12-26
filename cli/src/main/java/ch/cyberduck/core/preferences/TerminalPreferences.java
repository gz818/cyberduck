/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

package ch.cyberduck.core.preferences;

import ch.cyberduck.cli.TerminalCertificateStore;
import ch.cyberduck.cli.TerminalHostKeyVerifier;
import ch.cyberduck.cli.TerminalLoginCallback;
import ch.cyberduck.cli.TerminalNotification;
import ch.cyberduck.cli.TerminalTransferErrorCallback;
import ch.cyberduck.cli.TerminalTransferPrompt;
import ch.cyberduck.core.Factory;
import ch.cyberduck.core.editor.DefaultEditorFactory;
import ch.cyberduck.core.i18n.RegexLocale;
import ch.cyberduck.core.local.ExecApplicationLauncher;
import ch.cyberduck.core.local.features.DefaultSymlinkFeature;
import ch.cyberduck.core.proxy.EnvironmentVariableProxyFinder;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferAction;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * @version $Id$
 */
public class TerminalPreferences extends MemoryPreferences {
    private static final Logger log = Logger.getLogger(TerminalPreferences.class);

    @Override
    protected void setFactories() {
        super.setFactories();

        defaults.put("factory.certificatestore.class", TerminalCertificateStore.class.getName());
        defaults.put("factory.logincallback.class", TerminalLoginCallback.class.getName());
        defaults.put("factory.hostkeycallback.class", TerminalHostKeyVerifier.class.getName());
        defaults.put("factory.transfererrorcallback.class", TerminalTransferErrorCallback.class.getName());
        defaults.put("factory.notification.class", TerminalNotification.class.getName());
        for(Transfer.Type t : Transfer.Type.values()) {
            defaults.put(String.format("factory.transferpromptcallback.%s.class", t.name()), TerminalTransferPrompt.class.getName());
        }
        switch(Factory.Platform.getDefault()) {
            case mac:
                break;
            case windows:
                break;
            case linux:
                defaults.put("factory.supportdirectoryfinder.class", UserHomeSupportDirectoryFinder.class.getName());
                defaults.put("factory.applicationresourcesfinder.class", StaticApplicationResourcesFinder.class.getName());
                defaults.put("factory.locale.class", RegexLocale.class.getName());
                defaults.put("factory.applicationlauncher.class", ExecApplicationLauncher.class.getName());
                defaults.put("factory.editorfactory.class", DefaultEditorFactory.class.getName());
                defaults.put("factory.proxy.class", EnvironmentVariableProxyFinder.class.getName());
                defaults.put("factory.symlink.class", DefaultSymlinkFeature.class.getName());
                break;
        }
    }

    @Override
    protected void setLogging() {
        defaults.put("logging.config", "log4j-cli.xml");
        defaults.put("logging", "fatal");

        super.setLogging();
    }

    @Override
    protected void setDefaults() {
        super.setDefaults();

        defaults.put("website.home", "http://duck.sh/");
        defaults.put("website.help", "http://help.duck.sh/");

        System.setProperty("jna.library.path", this.getProperty("java.library.path"));

        switch(Factory.Platform.getDefault()) {
            case mac: {
                defaults.put("connection.ssl.keystore.type", "KeychainStore");
                defaults.put("connection.ssl.keystore.provider", "Apple");

                break;
            }
            case windows: {
                defaults.put("connection.ssl.keystore.type", "Windows-MY");
                defaults.put("connection.ssl.keystore.provider", "SunMSCAPI");

                break;
            }
            case linux: {
                try {
                    final Process echo = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", "echo ~"});
                    defaults.put("local.user.home", StringUtils.strip(IOUtils.toString(echo.getInputStream())));
                }
                catch(IOException e) {
                    log.warn("Failure determining user home with `echo ~`");
                }
                defaults.put("ssh.authentication.agent.enable", String.valueOf(false));
                // Lowercase folder names to use when looking for profiles and bookmarks in user support directory
                defaults.put("bookmarks.folder.name", "bookmarks");
                defaults.put("profiles.folder.name", "profiles");
                defaults.put("connection.ssl.securerandom", "NativePRNGNonBlocking");

                break;
            }
        }
        defaults.put("local.normalize.prefix", String.valueOf(true));
        defaults.put("connection.login.name", System.getProperty("user.name"));

        // Disable transfer filters
        defaults.put("queue.download.skip.enable", "false");
        defaults.put("queue.upload.skip.enable", "false");

        defaults.put("queue.copy.action", TransferAction.comparison.name());
        defaults.put("queue.copy.reload.action", TransferAction.comparison.name());
    }

    @Override
    public String getProperty(final String property) {
        final String env = System.getenv(property);
        if(null == env) {
            final String system = System.getProperty(property);
            if(null == system) {
                return super.getProperty(property);
            }
            return system;
        }
        return env;
    }
}
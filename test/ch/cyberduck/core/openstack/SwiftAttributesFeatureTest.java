package ch.cyberduck.core.openstack;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginConnectionService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.NotfoundException;

import org.junit.Test;

import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @version $Id$
 */
public class SwiftAttributesFeatureTest extends AbstractTestCase {

    @Test(expected = NotfoundException.class)
    public void testFindNotFound() throws Exception {
        final SwiftSession session = new SwiftSession(
                new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com",
                        new Credentials(
                                properties.getProperty("rackspace.key"), properties.getProperty("rackspace.secret")
                        )));
        new LoginConnectionService(new DisabledLoginController(), new DisabledHostKeyCallback(),
                new DisabledPasswordStore(), new DisabledProgressListener()).connect(session, Cache.<Path>empty());
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("DFW");
        final Path test = new Path(container, UUID.randomUUID().toString() + ".txt", EnumSet.of(Path.Type.file));
        final SwiftAttributesFeature f = new SwiftAttributesFeature(session);
        f.find(test);
    }

    @Test
    public void testFind() throws Exception {
        final SwiftSession session = new SwiftSession(
                new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com",
                        new Credentials(
                                properties.getProperty("rackspace.key"), properties.getProperty("rackspace.secret")
                        )));
        new LoginConnectionService(new DisabledLoginController(), new DisabledHostKeyCallback(),
                new DisabledPasswordStore(), new DisabledProgressListener()).connect(session, Cache.<Path>empty());
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("DFW");
        final Path test = new Path(container, UUID.randomUUID().toString() + ".txt", EnumSet.of(Path.Type.file));
        new SwiftTouchFeature(session).touch(test);
        final String v = UUID.randomUUID().toString();
        final PathAttributes attributes = new SwiftAttributesFeature(session).find(test);
        assertEquals(0L, attributes.getSize());
        assertEquals(EnumSet.of(Path.Type.file), test.getType());
        assertEquals("d41d8cd98f00b204e9800998ecf8427e", attributes.getChecksum());
        assertNotNull(attributes.getModificationDate());
        new SwiftDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginController());
        session.close();
    }

    @Test
    public void testFindContainer() throws Exception {
        final SwiftSession session = new SwiftSession(
                new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com",
                        new Credentials(
                                properties.getProperty("rackspace.key"), properties.getProperty("rackspace.secret")
                        )));
        new LoginConnectionService(new DisabledLoginController(), new DisabledHostKeyCallback(),
                new DisabledPasswordStore(), new DisabledProgressListener()).connect(session, Cache.<Path>empty());
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("DFW");
        final PathAttributes attributes = new SwiftAttributesFeature(session).find(container);
        assertEquals(EnumSet.of(Path.Type.volume, Path.Type.directory), container.getType());
        session.close();
    }

    @Test
    public void testFindPlaceholder() throws Exception {
        final SwiftSession session = new SwiftSession(
                new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com",
                        new Credentials(
                                properties.getProperty("rackspace.key"), properties.getProperty("rackspace.secret")
                        )));
        new LoginConnectionService(new DisabledLoginController(), new DisabledHostKeyCallback(),
                new DisabledPasswordStore(), new DisabledProgressListener()).connect(session, Cache.<Path>empty());
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("DFW");
        final Path test = new Path(container, "placeholder", EnumSet.of(Path.Type.directory, Path.Type.placeholder));
        final PathAttributes attributes = new SwiftAttributesFeature(session).find(test);
        assertEquals(-1L, attributes.getSize());
        session.close();
    }
}

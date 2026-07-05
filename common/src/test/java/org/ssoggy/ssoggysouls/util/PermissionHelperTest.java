package org.ssoggy.ssoggysouls.util;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PermissionHelperTest {

    @Test
    void testIsTrustedAdmin_EmptyList() {
        assertFalse(PermissionHelper.isTrustedAdmin("uuid1", "name1", Collections.emptyList()));
    }

    @Test
    void testIsTrustedAdmin_NullList() {
        assertFalse(PermissionHelper.isTrustedAdmin("uuid1", "name1", null));
    }

    @Test
    void testIsTrustedAdmin_TrustedByUuid() {
        assertTrue(PermissionHelper.isTrustedAdmin("uuid1", "name1", List.of("uuid1", "other-name")));
    }

    @Test
    void testIsTrustedAdmin_TrustedByName() {
        assertTrue(PermissionHelper.isTrustedAdmin("uuid1", "name1", List.of("other-uuid", "name1")));
    }

    @Test
    void testIsTrustedAdmin_NotTrusted() {
        assertFalse(PermissionHelper.isTrustedAdmin("uuid1", "name1", List.of("other-uuid", "other-name")));
    }
}

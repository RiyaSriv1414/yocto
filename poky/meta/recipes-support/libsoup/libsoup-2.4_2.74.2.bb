SUMMARY = "An HTTP library implementation in C"
DESCRIPTION = "libsoup is an HTTP client/server library for GNOME. It uses GObjects \
and the glib main loop, to integrate well with GNOME applications."
HOMEPAGE = "https://wiki.gnome.org/Projects/libsoup"
BUGTRACKER = "https://bugzilla.gnome.org/"
SECTION = "x11/gnome/libs"
LICENSE = "LGPL-2.0-only"
LIC_FILES_CHKSUM = "file://COPYING;md5=5f30f0716dfdd0d91eb439ebec522ec2"

DEPENDS = "glib-2.0 glib-2.0-native libxml2 sqlite3 libpsl"

SHRT_VER = "${@d.getVar('PV').split('.')[0]}.${@d.getVar('PV').split('.')[1]}"

SRC_URI = "${GNOME_MIRROR}/libsoup/${SHRT_VER}/libsoup-${PV}.tar.xz \
           file://0001-meson.build-set-c_std-to-gnu99.patch \
           file://CVE-2024-52530.patch \
           file://CVE-2024-52532-1.patch \
           file://CVE-2024-52532-2.patch \
           file://CVE-2024-52532-3.patch \
           file://CVE-2024-52531-1.patch \
           file://CVE-2024-52531-2.patch \
           file://CVE-2025-32906-1.patch \
           file://CVE-2025-32906-2.patch \
           file://CVE-2025-32909.patch \
          "
SRC_URI[sha256sum] = "f0a427656e5fe19e1df71c107e88dfa1b2e673c25c547b7823b6018b40d01159"

CVE_PRODUCT = "libsoup"

S = "${WORKDIR}/libsoup-${PV}"

inherit meson gettext pkgconfig upstream-version-is-even gobject-introspection gtk-doc

UPSTREAM_CHECK_REGEX = "libsoup-(?P<pver>2(\.(?!99)\d+)+)\.tar"

GIR_MESON_ENABLE_FLAG = 'enabled'
GIR_MESON_DISABLE_FLAG = 'disabled'

# libsoup-gnome is entirely deprecated and just stubs in 2.42 onwards. Disable by default.
PACKAGECONFIG ??= ""
PACKAGECONFIG[gnome] = "-Dgnome=true,-Dgnome=false"
PACKAGECONFIG[gssapi] = "-Dgssapi=enabled,-Dgssapi=disabled,krb5"

EXTRA_OEMESON:append = " -Dvapi=disabled -Dtls_check=false"

GTKDOC_MESON_OPTION = "gtk_doc"

# When built without gnome support, libsoup-2.4 will contain only one shared lib
# and will therefore become subject to renaming by debian.bbclass. Prevent
# renaming in order to keep the package name consistent regardless of whether
# gnome support is enabled or disabled.
DEBIAN_NOAUTONAME:${PN} = "1"

# glib-networking is needed for SSL, proxies, etc.
RRECOMMENDS:${PN} = "glib-networking"

BBCLASSEXTEND = "native nativesdk"

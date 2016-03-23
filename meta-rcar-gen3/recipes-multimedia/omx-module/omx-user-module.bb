DESCRIPTION = "OMX Media Components R-Car Gen3"
LICENSE = "CLOSED"
require include/omx-options.inc

DEPENDS = " \
    kernel-module-mmngr mmngr-user-module \
    vspmif-user-module kernel-module-vspmif \
    kernel-module-vspm kernel-module-vsp2driver \
"

# Task Control. Compile is not performed when not installing OMX Video and Audio Libs.
# Note) dummy-omx-user-module.inc does not exist.
INCLUDE_FILE = '${@base_conditional("USE_OMX_COMMON", "1", "dummy", "deltask", d )}'
include ${INCLUDE_FILE}-omx-user-module.inc

DEPENDS += '${@base_conditional("USE_VIDEO_OMX", "1", "kernel-module-uvcs", "", d )}'

inherit autotools

PACKAGE_ARCH = "${MACHINE_ARCH}"

# SRC file name
SRC_URI_OMX = '${@base_conditional("USE_OMX_COMMON", "1", "file://RTM0AC0000XCMCTL30SL40C.tar.bz2;unpack=0", "", d )}'
SRC_URI_VCMND = '${@base_conditional("USE_VIDEO_DEC", "1", "file://RTM0AC0000XVCMND30SL40C.tar.bz2;unpack=0", "", d )}'
SRC_URI_H264D = '${@base_conditional("USE_H264D_OMX", "1", "file://RTM0AC0000XV264D30SL40C.tar.bz2", "", d )}'
SRC_URI_H265D = '${@base_conditional("USE_H265D_OMX", "1", "file://RTM0AC0000XV265D30SL40C.tar.bz2", "", d )}'
SRC_URI_MPEG2D = '${@base_conditional("USE_MPEG2D_OMX", "1", "file://RTM0AC0000XVM2VD30SL40C.tar.bz2", "", d )}'
SRC_URI_ACMND = '${@base_conditional("USE_AUDIO_OMX", "1", "file://RTM0AC0000XACMND30SL40C.tar.gz", "", d )}'
SRC_URI_AACLC = '${@base_conditional("USE_AACLCD_OMX", "1", "file://RTM0AC0000XAAACD30SL40C.tar.gz", "", d )}'
SRC_URI_AACMZ = '${@base_conditional("USE_AACLC_MDW", "1", "file://RTM0AC0000ADAACMZ1SL40C.tar.gz", "", d )}'

SRC_URI = " \
    ${SRC_URI_OMX} \
    ${SRC_URI_VCMND} \
    ${SRC_URI_H264D} \
    ${SRC_URI_H265D} \
    ${SRC_URI_MPEG2D} \
    ${SRC_URI_ACMND} \
    ${SRC_URI_AACLC} \
    ${SRC_URI_AACMZ} \
"

# SRC directory name
OMX_COMMON_SRC = '${@base_conditional("USE_OMX_COMMON", "1", "RTM0AC0000XCMCTL30SL40C", "", d )}'
OMX_VIDEO_DEC_COMMON_SRC = '${@base_conditional("USE_VIDEO_DEC", "1", "RTM0AC0000XVCMND30SL40C", "", d )}'

OMX_H264_DEC_SRC = '${@base_conditional("USE_H264D_OMX", "1", "RTM0AC0000XV264D30SL40C", "", d )}'
OMX_H265_DEC_SRC = '${@base_conditional("USE_H265D_OMX", "1", "RTM0AC0000XV265D30SL40C", "", d )}'
OMX_MPEG2_DEC_SRC = '${@base_conditional("USE_MPEG2D_OMX", "1", "RTM0AC0000XVM2VD30SL40C", "", d )}'

OMX_VIDEO_SRC_LIST = " \
    ${OMX_COMMON_SRC} \
    ${OMX_VIDEO_DEC_COMMON_SRC} \
    ${OMX_H264_DEC_SRC} \
    ${OMX_H265_DEC_SRC} \
    ${OMX_MPEG2_DEC_SRC} \
"

AAC_MIDDLEWARE_SRC = "RTM0AC0000ADAACMZ1SL40C"

OMX_AUDIO_COMMON_SRC = '${@base_conditional("USE_AUDIO_OMX", "1", "RTM0AC0000XACMND30SL40C", "", d )}'
OMX_AACLC_DEC_SRC = '${@base_conditional("USE_AACLCD_OMX", "1", "RTM0AC0000XAAACD30SL40C", "", d )}'

OMX_AUDIO_SRC_LIST = " \
    ${OMX_AUDIO_COMMON_SRC} \
    ${OMX_AACLC_DEC_SRC} \
"

S = "${WORKDIR}/omx/"

# Create ${S} directory
do_unpack_prepend() {
    os.system("install -d ${S}")
}

do_unpack_append() {
    bb.build.exec_func('setup_build_tree', d)
}

setup_build_tree() {
    for omxmc in ${OMX_COMMON_SRC} ${OMX_VIDEO_DEC_COMMON_SRC}
    do
        tar xf ${WORKDIR}/${omxmc}.tar.bz2 -C ${WORKDIR}
        tar xf ${WORKDIR}/${omxmc}.tar.bz2 ${omxmc}/src --strip=2 -C ${S}
        tar xf ${WORKDIR}/${omxmc}.tar.bz2 ${omxmc}/include --strip=1 -C ${S}
    done
}

B = "${S}"

EXTRA_OECONF = "OMXR_DEFAULT_CONFIG_FILE_NAME=${sysconfdir}/omxr/omxr_config_base.txt"

do_configure() {
    chmod u+x autogen.sh
    ./autogen.sh
    oe_runconf
}

do_install_omx_video() {
    cd ${D}${libdir}
    for omxmc in ${OMX_VIDEO_SRC_LIST}
    do
        src="${WORKDIR}/${omxmc}"
        install -m 755 ${src}/${baselib}/lib*.so.* ${D}${libdir}
        install -m 644 ${src}/include/*.h ${D}${includedir}
        install -m 644 ${src}/config/*.txt ${D}${sysconfdir}/omxr
    done

    if [ "X${USE_OMX_COMMON}" = "X1" ] ; then
        ln -s libomxr_core.so.3.0.0 libomxr_core.so.3
        ln -s libomxr_core.so.3 libomxr_core.so

        ln -s libomxr_mc_cmn.so.3.0.0 libomxr_mc_cmn.so.3
        ln -s libomxr_mc_cmn.so.3 libomxr_mc_cmn.so
    fi

    if [ "X${USE_VIDEO_DEC}" = "X1" ] ; then
        ln -s libomxr_mc_vcmn.so.3.0.0 libomxr_mc_vcmn.so.3
        ln -s libomxr_mc_vcmn.so.3 libomxr_mc_vcmn.so

        ln -s libomxr_mc_vdcmn.so.3.0.0 libomxr_mc_vdcmn.so.3
        ln -s libomxr_mc_vdcmn.so.3 libomxr_mc_vdcmn.so

        ln -s libuvcs_dec.so.3.0.0 libuvcs_dec.so.3
        ln -s libuvcs_dec.so.3 libuvcs_dec.so
    fi

    if [ "X${USE_H264D_OMX}" = "X1" ]; then
        ln -s libomxr_mc_h264d.so.3.0.0 libomxr_mc_h264d.so.3
        ln -s libomxr_mc_h264d.so.3 libomxr_mc_h264d.so

        ln -s libuvcs_avcd.so.3.0.0 libuvcs_avcd.so.3
        ln -s libuvcs_avcd.so.3 libuvcs_avcd.so
    fi

    if [ "X${USE_H265D_OMX}" = "X1" ]; then
        ln -s libomxr_mc_hevd.so.3.0.0 libomxr_mc_hevd.so.3
        ln -s libomxr_mc_hevd.so.3 libomxr_mc_hevd.so

        ln -s libuvcs_hevd.so.3.0.0 libuvcs_hevd.so.3
        ln -s libuvcs_hevd.so.3 libuvcs_hevd.so
    fi

    if [ "X${USE_MPEG2D_OMX}" = "X1" ]; then
        ln -s libomxr_mc_m2vd.so.3.0.0 libomxr_mc_m2vd.so.3
        ln -s libomxr_mc_m2vd.so.3 libomxr_mc_m2vd.so

        ln -s libuvcs_m2vd.so.3.0.0 libuvcs_m2vd.so.3
        ln -s libuvcs_m2vd.so.3 libuvcs_m2vd.so
    fi
}

do_install_audio_middleware() {
    cd ${D}${libdir}
    if [ "X${USE_AACLC_MDW}" = "X1" ]; then
        install -m 755 ${WORKDIR}/${AAC_MIDDLEWARE_SRC}/${baselib}/libAACDLA_L.so.3.0 \
        ${D}${libdir}
        install -m 644 ${WORKDIR}/${AAC_MIDDLEWARE_SRC}/include/*.h ${D}${includedir}

        ln -s libAACDLA_L.so.3.0 libAACDLA_L.so.3
        ln -s libAACDLA_L.so.3 libAACDLA_L.so
    fi
}

do_install_omx_audio() {
    cd ${D}${libdir}
    for omxmc in ${OMX_AUDIO_SRC_LIST}
    do
        src="${WORKDIR}/${omxmc}/"
        install -m 755 ${src}/${baselib}/lib*.so.* \
        ${D}${libdir}
        if [ -d ${src}/include ]; then
            install -m 644 ${src}/include/*.h ${D}${includedir}
        fi
        install -m 644 ${src}/config/*.txt ${D}${sysconfdir}/omxr
    done

    if [ "X${USE_AUDIO_OMX}" = "X1" ]; then
        ln -s libomxr_mc_acmn.so.3.0.0 libomxr_mc_acmn.so.3
        ln -s libomxr_mc_acmn.so.3 libomxr_mc_acmn.so
    fi

    if [ "X${USE_AACLCD_OMX}" = "X1" ]; then
        ln -s libomxr_mc_aacd.so.3.0.0 libomxr_mc_aacd.so.3
        ln -s libomxr_mc_aacd.so.3 libomxr_mc_aacd.so
    fi
}

do_install () {
    if [ "X${USE_OMX_COMMON}" = "X1" ]; then
        oe_runmake 'DESTDIR=${D}' install
        # Info dir listing isn't interesting at this point so remove it if it exists.
        if [ -e "${D}${infodir}/dir" ]; then
                rm -f ${D}${infodir}/dir
        fi
    fi
}

do_install_append() {
    install -d ${D}${libdir}
    install -d ${D}${includedir}
    if [ "X${USE_OMX_COMMON}" = "X1" ]; then
        install -d ${D}${sysconfdir}/omxr
    fi

    do_install_omx_video
    do_install_audio_middleware
    do_install_omx_audio
}

INSANE_SKIP_${PN}= "already-stripped"
INSANE_SKIP_${PN} += "dev-so"

INHIBIT_SYSROOT_STRIP = "1"

FILES_${PN} += " \
    ${libdir}/*.so \
"

FILES_${PN}-dev = " \
    ${includedir} \
    ${libdir}/*.la \
"

RDEPENDS_${PN} += "mmngr-user-module vspmif-user-module"
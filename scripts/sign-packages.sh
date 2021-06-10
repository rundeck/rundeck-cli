#!/usr/bin/env bash
#/ Sign the RPMs and Debs in the dist dir ...
#/ usage: [dist dir]
#/ define env vars:
#/ USE_GPGV2=true if you are using gpg v2, otherwise v1 is assumed
#/ SIGNING_KEYID=<id of signing key>
#/ SIGNING_PASSWORD=<key password>
#/ GPG_PATH=/path/to/.gnupg  (may be empty dir if you use SIGNING_KEY_B64)
#/ SIGNING_KEY_B64=<base64 encoded ascii-armored private gpg key> (optional)

set -euo pipefail
IFS=$'\n\t'

source "packaging/scripts/signing-helpers.sh"

usage() {
    grep   '^#/' < "$0" | cut -c4- # prints the #/ lines above as usage info
}
die() {
    echo >&2 "$@"
    exit 2
}

main() {
    check_env
    check_key
    if isgpg2; then
        echo "gpg v2 detected"
        sign_rpms_gpg2
    else
        echo "gpg v2 not detected"
        sign_rpms
    fi
    sign_debs
}
main

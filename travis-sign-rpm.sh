#!/usr/bin/env bash
#/ Sign the RPMs in the dist dir ...
#/ usage: [dist dir]
#/ define env vars:
#/ SIGNING_KEYID=<id of signing key>
#/ SIGNING_PASSWORD=<key password>
#/ GPG_PATH=/path/to/.gnupg  (may be empty dir if you use SIGNING_KEY_B64)
#/ SIGNING_KEY_B64=<base64 encoded ascii-armored private gpg key> (optional)

set -euo pipefail
IFS=$'\n\t'
readonly ARGS=("$@")

DIST_DIR=${ARGS[0]?dist dir is required}
KEYID=${SIGNING_KEYID:-}
PASSWORD=${SIGNING_PASSWORD:-}
GPG_PATH=${GPG_PATH:-}
SIGNING_KEY_B64=${SIGNING_KEY_B64:-}


usage() {
      grep '^#/' <"$0" | cut -c4- # prints the #/ lines above as usage info
}
die(){
    echo >&2 "$@" ; exit 2
}

check_env(){
    if test -z "$DIST_DIR"; then
        die "arg DIST_DIR was not set"
    fi
    if test -z "$KEYID"; then
        die "ENV var SIGNING_KEYID was not set"
    fi
    if test -z "$PASSWORD"; then
        die "ENV var SIGNING_PASSWORD was not set"
    fi
    if test -z "$GPG_PATH"; then
        die "ENV var GPG_PATH was not set"
    fi

    which gpg || die "gpg not found"
    which rpm || die "rpm not found"
    which expect || die "expect not found"

}
check_key(){
  if [ -n "${SIGNING_KEY_B64}" ] ; then
    echo "${SIGNING_KEY_B64}" | base64 -d | GNUPGHOME="$GPG_PATH" gpg --import --batch
  fi
}
list_rpms(){
    local FARGS=("$@")
    local DIR=${FARGS[0]}
    ls $DIR/*.rpm
}

sign_rpms(){
    local RPMS=$(list_rpms $DIST_DIR)
    export GNUPGHOME=$GPG_PATH
    expect - -- $GPG_PATH $SIGNING_KEYID $SIGNING_PASSWORD  <<END
spawn rpm --define "_gpg_name [lindex \$argv 1]" --define "_gpg_path [lindex \$argv 0]" --define "__gpg_sign_cmd %{__gpg} gpg --force-v3-sigs --digest-algo=sha1 --batch --no-verbose --no-armor --passphrase-fd 3 --no-secmem-warning -u \"%{_gpg_name}\" -sbo %{__signature_filename} %{__plaintext_filename}" --addsign $RPMS
expect {
    -re "Enter pass *phrase: *" { log_user 0; send -- "[lindex \$argv 2]\r"; log_user 1; }
    eof { catch wait rc; exit [lindex \$rc 3]; }
    timeout { close; exit; }
}
expect {
eof { catch wait rc; exit [lindex \$rc 3]; }
timeout close
}
END


}

main() {
    check_env
    check_key
    sign_rpms
}
main


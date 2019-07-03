# how to add/updated encrypted gpg to travis

To use the rpm signing script, a gpg secring.gpg is required.

We encrypt this file for Travis-ci, and add it to git. **Be sure not to add the unencrypted file to git**

It is recommended to put it in the `scratch/` dir which is already in the .gitignore and will not be added to git.

## prepare tar file

put the secring.gpg into a directory "gpgdir"

	mkdir scratch/gpgdir
    cd scratch/gpgdir
    # create or copy the secring.gpg + pubring.gpg here
    tar cvf ../gpg.tar *

## encrypt file

Use the `travis` cli tool, you can install with `gem install travis`.

    travis encrypt-file scratch/gpg.tar gpg.tar.enc
    git add gpg.tar.enc
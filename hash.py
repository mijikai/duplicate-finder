#!/usr/bin/python3
import os
import hashlib
import tkinter
from tkinter import ttk


def digest(filename, meth=hashlib.sha1):
    """Calculates the hexadecimal hash of the ``filename`` using the ``meth``
    from hashlib."""
    with open(filename, 'rb') as f:
        hexhash = meth(f.read()).hexdigest()
    return hexhash


def list_files(path, followlinks=False):
    """Returns a generator that list the files containing in ``path``
    recursively.

    path - the directory to be transverse
    followlinks - follow the symbolic link if it is True. Default is True."""

    path = os.path.abspath(path)
    for dirpath, _, filenames in os.walk(path, followlinks=followlinks):
        for filename in filenames:
            yield os.path.join(dirpath, filename)


def hash_and_call(filepaths, meth=hashlib.sha1, callback=None):
    """Computes the hash for each of the filepaths then call the callback. If
    the filepath is a directory or does not exist, it will be ignored.

    filepaths - an iterable containing the file path in string.
    meth - algorithm to compute the hash of the file. Defaults to sha1.
    callback - called everytime the hash is computed successfully. The callback
        must accept two arguments. The first argument is the hash of the file
        and the second is the filepath. Defaults to None.

    During the call, one must not change the working directory if the filepaths
    are relative.
    """

    if callback == None:
        callback = lambda filehash, filename: None

    for path in filepaths:
        try:
            computed_hash = digest(path)
        except IOError:
            pass
        else:
            callback(computed_hash, path)


class DuplicateFileFinder(object):
    """Find all the duplicate files inside the directory and its
    subdirectories. If the file is a symbolic link, the filepath that will be
    included is the real path. Thus, if two files point to the same file, the
    path will be included if it is not yet included.
    """

    def __init__(self, path=None):
        if path is not None:
            self.get_duplicates(path)

    def get_duplicates(self, path):
        self._path = os.path.realpath(path)
        self._groups = {}
        hash_and_call(list_files(path), callback=self._add_file)
        self.groups = list(filter(lambda filenames: len(filenames) > 1,
            self._groups.values()))
        del self._groups
        return self.groups

    def _add_file(self, filehash, filename):
        """The callback that will be pass to hash_and_call function."""
        self._groups.setdefault(filehash,
                set()).add(os.path.realpath(filename))



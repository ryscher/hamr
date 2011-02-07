#! /usr/bin/python

"""This is a prototype for the HAMR metadata enhancement process"""

import urllib
from xml.dom.minidom import parse, parseString

class Harvester(object):

    def __init__(self, url, form):
        """Initialize with url?"""
        self.url = url
        self.form = form

    def get_record(self, identifier):
        """Take an identifier and grab the record"""
        x = urllib.urlopen(self.url + identifier)
        return x.read()

    def handle_metadata(self, record):
        r = {}
        if self.form == 'xml':
            dom = parseString(record)
            r['title'] = self._getText(dom.getElementsByTagName(
                        "ArticleTitle")[0].childNodes)
            authors = dom.getElementsByTagName("Author")
            for author in authors:
                # extract out the text elements from each author then push
                # into the metadata format
                print author
                a = self._getText(author.childNodes)
                print author.childNodes.length
            return r

    def dc_matching(self):
        """Walk through fields ... """
        pass

    def string_scoring(self, s1, s2):
        """Compare 2 strings and return a similarity score"""
        pass

    def output_matches(self):
        """output our xml for styling"""
        return 

    def _getText(self, nodelist):
        rc = []
        for node in nodelist:
            if node.nodeType == node.TEXT_NODE:
                rc.append(node.data)
        return ''.join(rc)



if __name__ == "__main__":
    u = ('http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=' +
            'pubmed&retmode=xml&id=')
    h = Harvester(u, 'xml')
    r = h.get_record('11748933')
    print h.handle_metadata(r)


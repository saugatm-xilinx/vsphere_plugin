"""
This script publishes the file on ivy according to the
command line inputs provided.
"""

import os
import sys
import shutil
import optparse
import xml.etree.ElementTree as xmlparser

def fail(msg):
    """ Function prints the error message and exits """
    print(msg)
    sys.exit(1)

def main():
    """ Starting point of script execution. Parses the inputs
        calls functions to copy images and generate json file. """
    try:
        parser = optparse.OptionParser(usage=
                                       """Usage: %prog [options]
                                          """,
                                       version="%prog 1.0")
        parser.add_option("-r", "--revision", dest='rev_tag',
                          help="revision to write files in")
        parser.add_option("-n", "--name", dest='file_name',
                          help="msi file name to publish")

        options, args = parser.parse_args()
        if ((len(args) > 1) or
            (options.rev_tag is None)):
            parser.print_help()
            fail("Exiting")
        if not options.rev_tag.startswith('v'):
            options.rev_tag = 'v' + options.rev_tag
        ret_val = os.system('hg clone /project/hg/incoming/ivybase')
        if ret_val != 0:
            fail("Not able to clone ivybase")
        curr_dir = os.getcwd()
        ivy_dir = curr_dir + '/ivybase'
        ivy_xml_file = curr_dir + '/ivy.xml'
        tree = xmlparser.parse(ivy_xml_file)
        root_node = tree.getroot()
        if options.file_name is None:
            options.file_name = 'installer.zip'
        name = os.path.splitext(os.path.basename(options.file_name))
        if (name[1] == '.msi'):
            rhs = 'msi'
            lhs = name[0]
        elif (name[1] == '.zip'):
            rhs = 'zip'
        if ((rhs == 'zip') or
            (rhs == 'msi')):
            for child in root_node:
                if child.tag == "info":
                    child.set('revision',options.rev_tag)
                if ((child.tag == "publications") and
                    (rhs == 'msi')):
                    for artifact in child.getchildren():
                        artifact.set('name',lhs)
                        artifact.set('ext','msi')
                        artifact.set('type','bin')
            tree.write(ivy_xml_file)
            ret_val = os.system(ivy_dir + '/scripts/ivy_publish ivy.xml')
            if ret_val != 0:
                fail("Not able to publish on ivy")
        ret_val = os.system('rm -rf ivybase')
        if ret_val != 0:
            fail("Not able to remove ivybase")
    except KeyError:
        fail("Image variant not determined. Exiting.")
    except OSError:
        fail("OS Environment error. Exiting.")
    except IOError:
        fail("Publishing to ivy failed. Exiting.")

if __name__ == '__main__':
    main()


/*---------------------------------------------------------------------------*\
  $Id$
  ---------------------------------------------------------------------------
  This software is released under a BSD-style license:

  Copyright (c) 2004-2006 Brian M. Clapper. All rights reserved.

  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are
  met:

  1.  Redistributions of source code must retain the above copyright notice,
      this list of conditions and the following disclaimer.

  2.  The end-user documentation included with the redistribution, if any,
      must include the following acknowlegement:

        "This product includes software developed by Brian M. Clapper
        (bmc@clapper.org, http://www.clapper.org/bmc/). That software is
        copyright (c) 2004-2006 Brian M. Clapper."

      Alternately, this acknowlegement may appear in the software itself,
      if wherever such third-party acknowlegements normally appear.

  3.  Neither the names "clapper.org", "clapper.org Java Utility Library",
      nor any of the names of the project contributors may be used to
      endorse or promote products derived from this software without prior
      written permission. For written permission, please contact
      bmc@clapper.org.

  4.  Products derived from this software may not be called "clapper.org
      Java Utility Library", nor may "clapper.org" appear in their names
      without prior written permission of Brian M.a Clapper.

  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
  NO EVENT SHALL BRIAN M. CLAPPER BE LIABLE FOR ANY DIRECT, INDIRECT,
  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
  NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
\*---------------------------------------------------------------------------*/

package org.clapper.util.text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * Static class containing miscellaneous text utility methods.
 *
 * @version <tt>$Revision$</tt>
 *
 * @author Copyright &copy; 2004-2006 Brian M. Clapper
 */
public final class TextUtil
{
    /*----------------------------------------------------------------------*\
                             Public Constants
    \*----------------------------------------------------------------------*/

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    private TextUtil()
    {
        // Can't be instantiated
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Convert a boolean string to a boolean value. This method is more
     * generous than <tt>java.lang.Boolean.booleanValue()</tt>. The following
     * strings (in upper or lower case) are recognized as <tt>true</tt>:
     * "1", "true", "yes", "y". The following
     * strings (in upper or lower case) are recognized as <tt>false</tt>:
     * "0", "false", "no", "n".
     *
     * @param s   string to convert
     *
     * @return <tt>true</tt> or <tt>false</tt>
     *
     * @throws IllegalArgumentException  string isn't a boolean
     */
    public static boolean booleanFromString (String s)
        throws IllegalArgumentException
    {
        boolean result;

        s = s.toLowerCase();

        if ((s.equals ("true") ||
             s.equals ("1")    ||
             s.equals ("yes")  ||
             s.equals ("y")))
        {
            result = true;
        }

        else if ((s.equals ("false") ||
                  s.equals ("0")     ||
                  s.equals ("no")    ||
                  s.equals ("n")))
        {
            result = false;
        }

        else
        {
            throw new IllegalArgumentException ("Bad boolean string: \"" +
                                                s + "\"");
        }

        return result;
    }

    /**
     * <p>Split a string on white space, into one or more strings. This
     * method is intended to be reminiscent of the corresponding perl or
     * awk <i>split()</i> function, though without regular expression
     * support. This version of <tt>split()</tt> does not preserve
     * empty strings. That is, the string "a:b::c", when split with a
     * ":" delimiter, yields three fields ("a", "b", "c"), since the
     * two adjacent ":" characters are treated as one delimiter. To
     * preserve empty strings, pass <tt>true</tt> as the
     * <tt>preserveEmptyFields</tt> parameter to the
     * {@link #split(String,boolean)} method.</p>
     *
     * <p>Note that the 1.4 JDK introduces a regular expression-based
     * <tt>split()</tt> method in the <tt>java.lang.String</tt> class.
     * This method does not use regular expressions.</p>
     *
     * @param s  the string to split
     *
     * @return an array of <tt>String</tt> objects
     *
     * @see #split(String,boolean)
     * @see #split(String,String)
     * @see #split(String,char)
     * @see #split(String,Collection)
     * @see #split(String,char,Collection)
     * @see #split(String,String,Collection)
     */
    public static String[] split (String s)
    {
        return split (s, (String) null);
    }

    /**
     * <p>Split a string on white space, into one or more strings. This
     * method is intended to be reminiscent of the corresponding perl or
     * awk <i>split()</i> function, though without regular expression
     * support.</p>
     *
     * <p>Note that the 1.4 JDK introduces a regular expression-based
     * <tt>split()</tt> method in the <tt>java.lang.String</tt> class.
     * This method does not use regular expressions.</p>
     *
     * @param s                   the string to split
     * @param preserveEmptyFields Whether to parse through empty tokens or
     *                            preserve them. For example, given the string
     *                            string "a:b::c" and a delimiter of ":",
     *                            if <tt>preserveEmptyStrings</tt> is
     *                            <tt>true</tt>, then this method will return
     *                            four strings, "a", "b", "", "c". If
     *                            <tt>preserveEmptyStrings</tt> is
     *                            <tt>false</tt>, then this method will return
     *                            three strings, "a", "b", "c" (since the
     *                            two adjacent ":" characters are treated as
     *                            one delimiter.)
     *
     * @return an array of <tt>String</tt> objects
     *
     * @see #split(String)
     * @see #split(String,String)
     * @see #split(String,char)
     * @see #split(String,Collection)
     * @see #split(String,char,Collection)
     * @see #split(String,String,Collection)
     */
    public static String[] split (String s, boolean preserveEmptyFields)
    {
        return split (s, (String) null, preserveEmptyFields);
    }

    /**
     * <p>Split a string on white space, into one or more strings. This
     * method is intended to be reminiscent of the corresponding perl or
     * awk <i>split()</i> function, though without regular expression
     * support. This version of <tt>split()</tt> does not preserve
     * empty strings. That is, the string "a:b::c", when split with a
     * ":" delimiter, yields three fields ("a", "b", "c"), since the
     * two adjacent ":" characters are treated as one delimiter. To
     * preserve empty strings, pass <tt>true</tt> as the
     * <tt>preserveEmptyFields</tt> parameter to the
     * {@link #split(String,Collection,boolean)} method.</p>
     *
     * <p>Note that the 1.4 JDK introduces a regular expression-based
     * <tt>split()</tt> method in the <tt>java.lang.String</tt> class.
     * This method does not use regular expressions.</p>
     *
     * @param s          the string to split
     * @param collection where to store the split strings
     *
     * @return the number of strings added to the collection
     *
     * @see #split(String,Collection,boolean)
     * @see #split(String)
     * @see #split(String,String)
     * @see #split(String,char)
     * @see #split(String,char,Collection)
     * @see #split(String,String,Collection)
     */
    public static int split (String s, Collection<String> collection)
    {
        return split (s, collection, false);
    }

    /**
     * <p>Split a string on white space, into one or more strings. This
     * method is intended to be reminiscent of the corresponding perl or
     * awk <i>split()</i> function, though without regular expression
     * support.</p>
     *
     * <p>Note that the 1.4 JDK introduces a regular expression-based
     * <tt>split()</tt> method in the <tt>java.lang.String</tt> class.
     * This method does not use regular expressions.</p>
     *
     * @param s                   the string to split
     * @param collection          where to store the split strings
     * @param preserveEmptyFields Whether to parse through empty tokens or
     *                            preserve them. For example, given the string
     *                            string "a:b::c" and a delimiter of ":",
     *                            if <tt>preserveEmptyStrings</tt> is
     *                            <tt>true</tt>, then this method will return
     *                            four strings, "a", "b", "", "c". If
     *                            <tt>preserveEmptyStrings</tt> is
     *                            <tt>false</tt>, then this method will return
     *                            three strings, "a", "b", "c" (since the
     *                            two adjacent ":" characters are treated as
     *                            one delimiter.)
     *
     * @return the number of strings added to the collection
     *
     * @see #split(String,Collection)
     * @see #split(String)
     * @see #split(String,String)
     * @see #split(String,char)
     * @see #split(String,char,Collection)
     * @see #split(String,String,Collection)
     */
    public static int split (String             s,
                             Collection<String> collection,
                             boolean            preserveEmptyFields)
    {
        return split (s, (String) null, collection, preserveEmptyFields);
    }

    /**
     * <p>Split a string into one or more strings, based on a delimiter.
     * This method is intended to be reminiscent of the corresponding perl
     * or awk <i>split()</i> function, though without regular expression
     * support. This version of <tt>split()</tt> does not preserve empty
     * strings. That is, the string "a:b::c", when split with a ":"
     * delimiter, yields three fields ("a", "b", "c"), since the two
     * adjacent ":" characters are treated as one delimiter. To preserve
     * empty strings, pass <tt>true</tt> as the
     * <tt>preserveEmptyFields</tt> parameter to the
     * {@link #split(String,char,boolean)} method.</p>
     *
     * <p>Note that the 1.4 JDK introduces a regular expression-based
     * <tt>split()</tt> method in the <tt>java.lang.String</tt> class.
     * This method does not use regular expressions.</p>
     *
     * @param s     the string to split
     * @param delim the delimiter
     *
     * @return an array of <tt>String</tt> objects
     *
     * @see #split(String,char,boolean)
     * @see #split(String)
     * @see #split(String,String)
     * @see #split(String,Collection)
     * @see #split(String,char,Collection)
     * @see #split(String,String,Collection)
     */
    public static String[] split (String s, char delim)
    {
        return split (s, "" + delim, false);
    }

    /**
     * <p>Split a string into one or more strings, based on a delimiter.
     * This method is intended to be reminiscent of the corresponding perl
     * or awk <i>split()</i> function, though without regular expression
     * support.</p>
     *
     * <p>Note that the 1.4 JDK introduces a regular expression-based
     * <tt>split()</tt> method in the <tt>java.lang.String</tt> class.
     * This method does not use regular expressions.</p>
     *
     * @param s                   the string to split
     * @param delim               the delimiter
     * @param preserveEmptyFields Whether to parse through empty tokens or
     *                            preserve them. For example, given the string
     *                            string "a:b::c" and a delimiter of ":",
     *                            if <tt>preserveEmptyStrings</tt> is
     *                            <tt>true</tt>, then this method will return
     *                            four strings, "a", "b", "", "c". If
     *                            <tt>preserveEmptyStrings</tt> is
     *                            <tt>false</tt>, then this method will return
     *                            three strings, "a", "b", "c" (since the
     *                            two adjacent ":" characters are treated as
     *                            one delimiter.)
     *
     * @return an array of <tt>String</tt> objects
     *
     * @see #split(String,char)
     * @see #split(String)
     * @see #split(String,String)
     * @see #split(String,Collection)
     * @see #split(String,char,Collection)
     * @see #split(String,String,Collection)
     */
    public static String[] split (String  s,
                                  char    delim,
                                  boolean preserveEmptyFields)
    {
        return split (s, String.valueOf (delim), preserveEmptyFields);
    }

    /**
     * <p>Split a string into one or more strings, based on a set of
     * delimiter. This method is intended to be reminiscent of the
     * corresponding perl or awk <i>split()</i> function, though without
     * regular expression support. This version of <tt>split()</tt> does
     * not preserve empty strings. That is, the string "a:b::c", when split
     * with a ":" delimiter, yields three fields ("a", "b", "c"), since the
     * two adjacent ":" characters are treated as one delimiter. To
     * preserve empty strings, pass <tt>true</tt> as the
     * <tt>preserveEmptyFields</tt> parameter to the
     * {@link #split(String,String,boolean)} method.</p>
     *
     * <p>Note that the 1.4 JDK introduces a regular expression-based
     * <tt>split()</tt> method in the <tt>java.lang.String</tt> class.
     * This method does not use regular expressions.</p>
     *
     * @param s         the string to split
     * @param delimSet  set of delimiters, or null to use white space
     *
     * @return an array of <tt>String</tt> objects
     *
     * @see #split(String,String,boolean)
     * @see #split(String)
     * @see #split(String,char)
     * @see #split(String,Collection)
     * @see #split(String,char,Collection)
     * @see #split(String,String,Collection)
     */
    public static String[] split (String s, String delimSet)
    {
        return split (s, delimSet, false);
    }

    /**
     * <p>Split a string into one or more strings, based on a set of
     * delimiter. This method is intended to be reminiscent of the
     * corresponding perl or awk <i>split()</i> function, though without
     * regular expression support.</p>
     *
     * <p>Note that the 1.4 JDK introduces a regular expression-based
     * <tt>split()</tt> method in the <tt>java.lang.String</tt> class.
     * This method does not use regular expressions.</p>
     *
     * @param s                   the string to split
     * @param delimSet            set of delimiters, or null to use white space
     * @param preserveEmptyFields Whether to parse through empty tokens or
     *                            preserve them. For example, given the string
     *                            string "a:b::c" and a delimiter of ":",
     *                            if <tt>preserveEmptyStrings</tt> is
     *                            <tt>true</tt>, then this method will return
     *                            four strings, "a", "b", "", "c". If
     *                            <tt>preserveEmptyStrings</tt> is
     *                            <tt>false</tt>, then this method will return
     *                            three strings, "a", "b", "c" (since the
     *                            two adjacent ":" characters are treated as
     *                            one delimiter.)
     *
     * @return an array of <tt>String</tt> objects
     *
     * @see #split(String,String)
     * @see #split(String)
     * @see #split(String,char)
     * @see #split(String,Collection)
     * @see #split(String,char,Collection)
     * @see #split(String,String,Collection)
     */
    public static String[] split (String  s,
                                  String  delimSet,
                                  boolean preserveEmptyFields)
    {
        String[]           result = null;
        StringTokenizer    tok;
        Collection<String> temp = new ArrayList<String>();

        if (delimSet == null)
            delimSet = " \t\n\r";

        tok = new StringTokenizer (s, delimSet, preserveEmptyFields);

        // Assume we'll never see the delimiter unless preserveEmptyFields is
        // set.

        boolean lastWasDelim = true;
        while (tok.hasMoreTokens())
        {
            String token = tok.nextToken();

            if (preserveEmptyFields &&
                (token.length() == 1) &&
                (delimSet.indexOf (token.charAt (0)) != -1))
            {
                if (lastWasDelim)
                    token = "";
                else
                {
                    lastWasDelim = true;
                    continue;
                }
            }

            else
            {
                lastWasDelim = false;
            }

            temp.add (token);
        }

        result = new String[temp.size()];
        temp.toArray (result);
        return result;
    }

    /**
     * <p>Split a string into one or more strings, based on a delimiter.
     * This method is intended to be reminiscent of the corresponding perl
     * or awk <i>split()</i> function, though without regular expression
     * support. This version of <tt>split()</tt> does not preserve empty
     * strings. That is, the string "a:b::c", when split with a ":"
     * delimiter, yields three fields ("a", "b", "c"), since the two
     * adjacent ":" characters are treated as one delimiter. To preserve
     * empty strings, pass <tt>true</tt> as the
     * <tt>preserveEmptyFields</tt> parameter to the
     * {@link #split(String,char,Collection,boolean)} method.</p>
     *
     * <p>Note that the 1.4 JDK introduces a regular expression-based
     * <tt>split()</tt> method in the <tt>java.lang.String</tt> class.
     * This method does not use regular expressions.</p>
     *
     * @param s          the string to split
     * @param delim      the delimiter
     * @param collection where to store the split strings
     *
     * @return the number of <tt>String</tt> objects added to the collection
     *
     * @see #split(String,char,Collection,boolean)
     * @see #split(String)
     * @see #split(String,char)
     * @see #split(String,Collection)
     * @see #split(String,String)
     * @see #split(String,String,Collection)
     */
    public static int split (String             s,
                             char               delim,
                             Collection<String> collection)
    {
        return split (s, String.valueOf (delim), collection);
    }

    /**
     * <p>Split a string into one or more strings, based on a delimiter.
     * This method is intended to be reminiscent of the corresponding perl
     * or awk <i>split()</i> function, though without regular expression
     * support.</p>
     *
     * <p>Note that the 1.4 JDK introduces a regular expression-based
     * <tt>split()</tt> method in the <tt>java.lang.String</tt> class.
     * This method does not use regular expressions.</p>
     *
     * @param s                   the string to split
     * @param delim               the delimiter
     * @param collection          where to store the split strings
     * @param preserveEmptyFields Whether to parse through empty tokens or
     *                            preserve them. For example, given the string
     *                            string "a:b::c" and a delimiter of ":",
     *                            if <tt>preserveEmptyStrings</tt> is
     *                            <tt>true</tt>, then this method will return
     *                            four strings, "a", "b", "", "c". If
     *                            <tt>preserveEmptyStrings</tt> is
     *                            <tt>false</tt>, then this method will return
     *                            three strings, "a", "b", "c" (since the
     *                            two adjacent ":" characters are treated as
     *                            one delimiter.)
     *
     * @return the number of <tt>String</tt> objects added to the collection
     *
     * @see #split(String,char,Collection)
     * @see #split(String)
     * @see #split(String,char)
     * @see #split(String,Collection)
     * @see #split(String,String)
     * @see #split(String,String,Collection)
     */
    public static int split (String             s,
                             char               delim,
                             Collection<String> collection,
                             boolean            preserveEmptyFields)
    {
        return split (s,
                      String.valueOf (delim),
                      collection,
                      preserveEmptyFields);
    }

    /**
     * <p>Split a string into one or more strings, based on a set of
     * delimiter. This method is intended to be reminiscent of the
     * corresponding perl or awk <i>split()</i> function, though without
     * regular expression support. This version of <tt>split()</tt> does
     * not preserve empty strings. That is, the string "a:b::c", when split
     * with a ":" delimiter, yields three fields ("a", "b", "c"), since the
     * two adjacent ":" characters are treated as one delimiter. To
     * preserve empty strings, pass <tt>true</tt> as the
     * <tt>preserveEmptyFields</tt> parameter to the
     * {@link #split(String,String,Collection,boolean)} method.</p>
     *
     * <p>Note that the 1.4 JDK introduces a regular expression-based
     * <tt>split()</tt> method in the <tt>java.lang.String</tt> class. This
     * method does not use regular expressions.</p> This version of
     * <tt>split()</tt> does not preserve empty strings. That is, the
     * string "a:b::c", when split with a ":" delimiter, yields three
     * fields ("a", "b", "c"), since the two adjacent ":" characters are
     * treated as one delimiter. To preserve empty strings, pass
     * <tt>true</tt> as the <tt>preserveEmptyFields</tt> parameter to the
     * {@link #split(String,String,Collection,boolean)} method.</p>
     *
     * @param s          the string to split
     * @param delimSet   set of delimiters
     * @param collection where to store the split strings
     *
     * @return the number of <tt>String</tt> objects added to the collection
     *
     * @see #split(String,String,Collection,boolean)
     * @see #split(String)
     * @see #split(String,char)
     * @see #split(String,Collection)
     * @see #split(String,String)
     * @see #split(String,char,Collection)
     */
    public static int split (String             s,
                             String             delimSet,
                             Collection<String> collection)
    {
        return split (s, delimSet, collection, false);
    }

    /**
     * <p>Split a string into one or more strings, based on a set of
     * delimiter. This method is intended to be reminiscent of the
     * corresponding perl or awk <i>split()</i> function, though without
     * regular expression support. This method uses a
     * <tt>StringTokenizer</tt> to do the actual work.</p>
     *
     * <p>Note that the 1.4 JDK introduces a regular expression-based
     * <tt>split()</tt> method in the <tt>java.lang.String</tt> class. This
     * method does not use regular expressions.</p> This version of
     * <tt>split()</tt> does not preserve empty strings. That is, the
     * string "a:b::c", when split with a ":" delimiter, yields three
     * fields ("a", "b", "c"), since the two adjacent ":" characters are
     * treated as one delimiter. To preserve empty strings, pass
     * <tt>true</tt> as the <tt>preserveEmptyFields</tt> parameter to the
     * {@link #split(String,String,Collection,boolean)} method.</p>
     *
     * @param s                   the string to split
     * @param delimSet            set of delimiters
     * @param collection          where to store the split strings
     * @param preserveEmptyFields Whether to parse through empty tokens or
     *                            preserve them. For example, given the string
     *                            string "a:b::c" and a delimiter of ":",
     *                            if <tt>preserveEmptyStrings</tt> is
     *                            <tt>true</tt>, then this method will return
     *                            four strings, "a", "b", "", "c". If
     *                            <tt>preserveEmptyStrings</tt> is
     *                            <tt>false</tt>, then this method will return
     *                            three strings, "a", "b", "c" (since the
     *                            two adjacent ":" characters are treated as
     *                            one delimiter.)
     *
     * @return the number of <tt>String</tt> objects added to the collection
     *
     * @see #split(String,String,Collection)
     * @see #split(String)
     * @see #split(String,char)
     * @see #split(String,Collection)
     * @see #split(String,String)
     * @see #split(String,char,Collection)
     */
    public static int split (String             s,
                             String             delimSet,
                             Collection<String> collection,
                             boolean            preserveEmptyFields)
    {
        String[] strs = split (s, delimSet, preserveEmptyFields);

        for (int i = 0; i < strs.length; i++)
            collection.add (strs[i]);

        return strs.length;
    }

    /**
     * Join a set of strings into one string, putting the specified delimiter
     * between adjacent strings.
     *
     * @param strings  the strings to be joined
     * @param delim    the delimiter string
     *
     * @return the joined string, or "" if the array is empty.
     *
     * @see #split(String,String)
     * @see #join(String[],char)
     */
    public static String join (String[] strings, String delim)
    {
        StringBuilder result = new StringBuilder();
        String        sep    = "";

        for (int i = 0; i < strings.length; i++)
        {
            result.append (sep);
            result.append (strings[i]);
            sep = delim;
        }

        return result.toString();
    }

    /**
     * Join a set of strings into one string, putting the specified delimiter
     * between adjacent strings. This version of <tt>join()</tt> supports the
     * new Java variable argument syntax.
     *
     * @param delim    the delimiter string
     * @param strings  the strings to be joined
     *
     * @return the joined string, or "" if the array is empty.
     *
     * @see #split(String,char)
     * @see #join(String[],String)
     */
    public static String join (String delim, String... strings)
    {
        return join (strings, delim);
    }

    /**
     * Join an array of strings into one string, putting the specified
     * delimiter between adjacent strings.
     *
     * @param strings  the strings to be joined
     * @param delim    the delimiter character
     *
     * @return the joined string, or "" if the array is empty.
     *
     * @see #split(String,char)
     * @see #join(String[],String)
     */
    public static String join (String[] strings, char delim)
    {
        return join (strings, "" + delim);
    }

    /**
     * Join an array of strings into one string, putting the specified
     * delimiter between adjacent strings, starting at a specified index.
     *
     * @param strings  the strings to be joined
     * @param start    starting index
     * @param end      one past the ending index
     * @param delim    the delimiter character
     *
     * @return the joined string, or "" if the array is empty.
     *
     * @throws ArrayIndexOutOfBoundsException bad value for <tt>start</tt>
     *                                        or <tt>end</tt>
     *
     * @see #split(String,char)
     * @see #join(String[],String)
     */
    public static String join (String[] strings,
                               int      start,
                               int      end,
                               char     delim)
    {
        return join (strings, start, end, "" + delim);
    }

    /**
     * Join an array of strings into one string, putting the specified
     * delimiter between adjacent strings, starting at a specified index.
     *
     * @param strings  the strings to be joined
     * @param start    starting index
     * @param end      one past the ending index
     * @param delim    the delimiter string
     *
     * @return the joined string, or "" if the array is empty.
     *
     * @throws ArrayIndexOutOfBoundsException bad value for <tt>start</tt>
     *                                        or <tt>end</tt>
     *
     * @see #split(String,char)
     * @see #join(String[],String)
     */
    public static String join (String[] strings,
                               int      start,
                               int      end,
                               String   delim)
    {
        StringBuilder result = new StringBuilder();
        String        sep    = "";

        while (start < end)
        {
            result.append (sep);
            result.append (strings[start]);
            sep = delim;
            start++;
        }

        return result.toString();
    }

    /**
     * Join a set of strings into one string, putting the specified delimiter
     * between adjacent strings. This version of <tt>join()</tt> supports the
     * new Java variable argument syntax.
     *
     * @param delim    the delimiter character
     * @param strings  the strings to be joined
     *
     * @return the joined string, or "" if the array is empty.
     *
     * @see #split(String,char)
     * @see #join(String[],String)
     */
    public static String join (char delim, String... strings)
    {
        return join (strings, "" + delim);
    }

    /**
     * Join a set of strings into one string, putting the specified delimiter
     * between adjacent strings.
     *
     * @param objects  A collection the items to be joined. This collection
     *                 can contain objects of any type; each object's
     *                 <tt>toString()</tt> method is called to produce the
     *                 string to be joined.
     * @param delim    the delimiter string
     *
     * @return the joined string, or "" if the collection is empty.
     *
     * @see #split(String,String,Collection)
     * @see #join(Collection,char)
     */
    public static String join (Collection<? extends Object> objects,
                               String delim)
    {
        String result = "";

        if (objects.size() > 0)
        {
            String[] array;
            int      i;
            Iterator it;

            i = 0;
            for (it = objects.iterator(); it.hasNext();)
            {
                Object o = it.next();
                if (o == null)
                    continue;

                i++;
            }

            array = new String[i];
            i = 0;
            for (it = objects.iterator(); it.hasNext();)
            {
                Object o = it.next();
                if (o == null)
                    continue;

                array[i++] = o.toString();
            }

            result = join (array, delim);
        }

        return result;
    }

    /**
     * Join a set of strings into one string, putting the specified delimiter
     * between adjacent strings.
     *
     * @param objects  A collection the items to be joined. This collection
     *                 can contain objects of any type; each object's
     *                 <tt>toString()</tt> method is called to produce the
     *                 string to be joined.
     * @param delim    the delimiter character
     *
     * @return the joined string, or "" if the collection is empty.
     *
     * @see #split(String,char,Collection)
     * @see #join(Collection,String)
     */
    public static String join (Collection<? extends Object> objects,
                               char                         delim)
    {
        return join (objects, "" + delim);
    }

    /**
     * Determine whether a given string is empty. A string is empty if it
     * is null, zero-length, or comprised entirely of white space. This method
     * is more efficient than calling <tt>s.trim().length()</tt>, because it
     * does not create a new string just to test its length.
     *
     * @param s  the string to test
     *
     * @return <tt>true</tt> if it's empty, <tt>false</tt> if not.
     */
    public static boolean stringIsEmpty (String s)
    {
        boolean isEmpty = true;

        if (s != null)
        {
            char[] chars = s.toCharArray();

            for (int i = 0; i < chars.length; i++)
            {
                if (! Character.isWhitespace (chars[i]))
                {
                    isEmpty = false;
                    break;
                }
            }
        }

        return isEmpty;
    }

    /**
     * Right justify a string in a fixed-width field, using blanks for
     * padding. If the string is already longer than the field width, it is
     * returned unchanged.
     *
     * @param s      the string
     * @param width  the desired field width
     *
     * @return a right-justified version of the string
     *
     * @see #rightJustifyString(String,int,char)
     * @see #leftJustifyString(String,int)
     * @see #centerString(String,int)
     */
    public static String rightJustifyString (String s, int width)
    {
        return rightJustifyString (s, width, ' ');
    }

    /**
     * Right justify a string in a fixed-width field, using the specified
     * character for padding. If the string is already longer than the
     * field width, it is returned unchanged.
     *
     * @param s      the string
     * @param width  the desired field width
     * @param c      the pad character
     *
     * @return a right-justified version of the string
     *
     * @see #rightJustifyString(String,int)
     * @see #leftJustifyString(String,int,char)
     * @see #centerString(String,int,char)
     */
    public static String rightJustifyString (String s, int width, char c)
    {
        StringBuffer  paddedString = new StringBuffer (width);
        int           paddingNeeded;
        int           len = s.length();

        paddingNeeded = (width < len) ? 0 : (width - len);

        for (int i = 0; i < paddingNeeded; i++)
            paddedString.append (c);

        paddedString.append (s);

        return paddedString.toString();
    }

    /**
     * Left justify a string in a fixed-width field, using blanks for
     * padding. If the string is already longer than the field width, it is
     * returned unchanged.
     *
     * @param s      the string
     * @param width  the desired field width
     *
     * @return a left-justified version of the string
     *
     * @see #leftJustifyString(String,int,char)
     * @see #rightJustifyString(String,int)
     * @see #centerString(String,int)
     */
    public static String leftJustifyString (String s, int width)
    {
        return leftJustifyString (s, width, ' ');
    }

    /**
     * Left justify a string in a fixed-width field, using the specified
     * character for padding. If the string is already longer than the
     * field width, it is returned unchanged.
     *
     * @param s      the string
     * @param width  the desired field width
     * @param c      the pad character
     *
     * @return a left-justified version of the string
     *
     * @see #leftJustifyString(String,int)
     * @see #rightJustifyString(String,int,char)
     * @see #centerString(String,int,char)
     */
    public static String leftJustifyString (String s, int width, char c)
    {
        StringBuffer  paddedString = new StringBuffer (width);
        int           paddingNeeded;
        int           len = s.length();

        paddingNeeded = (width < len) ? 0 : (width - len);
        paddedString.append (s);

        for (int i = 0; i < paddingNeeded; i++)
            paddedString.append (c);

        return paddedString.toString();
    }

    /**
     * Center a string in a fixed-width field, using blanks for padding. If
     * the string is already longer than the field width, it is returned
     * unchanged.
     *
     * @param s      the string
     * @param width  the desired field width
     *
     * @return a centered version of the string
     *
     * @see #centerString(String,int,char)
     * @see #rightJustifyString(String,int)
     * @see #leftJustifyString(String,int)
     */
    public static String centerString (String s, int width)
    {
        return centerString (s, width, ' ');
    }

    /**
     * Center a string in a fixed-width field, using the specified
     * character for padding. If the string is already longer than the
     * field width, it is returned unchanged.
     *
     * @param s      the string
     * @param width  the desired field width
     * @param c      the pad character
     *
     * @return a right-justified version of the string
     *
     * @see #centerString(String,int,char)
     * @see #leftJustifyString(String,int,char)
     * @see #rightJustifyString(String,int,char)
     */
    public static String centerString (String s, int width, char c)
    {
        StringBuffer  paddedString = new StringBuffer (width);
        int           paddingNeeded;
        int           len = s.length();
        int           frontPadding;
        int           tailPadding;
        int           i;

        paddingNeeded = (width < len) ? 0 : (width - len);
        i = paddingNeeded / 2;
        frontPadding = i;
        tailPadding  = i + (paddingNeeded % 2);

        for (i = 0; i < frontPadding; i++)
            paddedString.append (c);

        paddedString.append (s);

        for (i = 0; i < tailPadding; i++)
            paddedString.append (c);

        return paddedString.toString();
    }
}

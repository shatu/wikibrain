package org.wikapidia.phrases;

import com.typesafe.config.Config;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.math.Fraction;
import org.wikapidia.conf.Configuration;
import org.wikapidia.conf.ConfigurationException;
import org.wikapidia.conf.Configurator;
import org.wikapidia.core.dao.DaoException;
import org.wikapidia.core.dao.LocalPageDao;
import org.wikapidia.core.lang.Language;
import org.wikapidia.core.lang.LanguageInfo;
import org.wikapidia.utils.WpIOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Loads phrase to page files from Indexes files from
 * http://www-nlp.stanford.edu/pubs/crosswikis-data.tar.bz2/
 * into a PhraseAnalyzer
 *
 * These files capture anchor phrase associated with web pages that link to Wikipedia.
 * Note that the pages with anchor phrase are not (usually) Wikipedia pages themselves.
 */
public class StanfordPhraseAnalyzer extends BasePhraseAnalyzer {
    private static final Logger LOG = Logger.getLogger(StanfordPhraseAnalyzer.class.getName());
    private static final LanguageInfo EN = LanguageInfo.getByLangCode("simple");

    private final File path;

    public StanfordPhraseAnalyzer(PhraseAnalyzerDao phraseDao, LocalPageDao pageDao, File path) {
        super(phraseDao, pageDao);
        this.path = path;
    }

    /**
     * Loads a single Stanford phrase file into the database.
     * This can safely be called for multiple files if it is chunked.
     * @throws IOException
     */
    @Override
    protected Iterable<BasePhraseAnalyzer.Entry> getCorpus() throws IOException, DaoException {
        return new Iterable<Entry>() {
            @Override
            public Iterator<Entry> iterator() {
                try {
                    return new Iter();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    protected class Iter implements Iterator<BasePhraseAnalyzer.Entry> {
        BufferedReader reader;
        long numLines = 0;
        long numLinesRetained = 0;
        BasePhraseAnalyzer.Entry buffer;
        boolean eof = false;

        public Iter() throws IOException {
            reader = WpIOUtils.openReader(path);
        }

        @Override
        public boolean hasNext() {
            fillBuffer();
            return (buffer != null);
        }

        @Override
        public BasePhraseAnalyzer.Entry next() {
            fillBuffer();
            BasePhraseAnalyzer.Entry tmp = buffer;
            buffer = null;
            return tmp;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        private void fillBuffer() {
            if (buffer != null || eof) {
                return;
            }
            while (!eof && buffer == null) {
                try {
                    parseNextLine();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (Exception e) {
                    LOG.log(Level.FINEST, "Error parsing line:", e);
                }
            }
        }

        private void parseNextLine() throws IOException {
            if (buffer != null) throw new IllegalStateException();
            String line = reader.readLine();
            if (line == null) {
                IOUtils.closeQuietly(reader);
                eof = true;
                return;
            }
            if (++numLines % 100000 == 0) {
                double p = 100.0 * numLinesRetained / numLines;
                LOG.info("processing line: " + numLines +
                        ", retained " + numLinesRetained +
                        "(" + new DecimalFormat("#.#").format(p) + "%)");
            }
            Record r = new Record(line);
            buffer = new BasePhraseAnalyzer.Entry(
                    Language.getByLangCode("en"),
                    r.article, r.phrase,
                    r.getNumEnglishLinks());
        }
    }


    /**
     * A single  entry corresponding to a line from a
     * dictionary.bz2 at http://www-nlp.stanford.edu/pubs/crosswikis-data.tar.bz2/.
     *
     * Major components of an entry are:
     * - textual phrase
     * - concept (a wikipedia article)
     * - A variety of flags
     */
    private static final Pattern MATCH_ENTRY = Pattern.compile("([^\t]*)\t([0-9.e-]+) ([^ ]*)(| (.*))$");

    class Record {
        String phrase;
        float fraction;
        String article;
        String flags[];

        Record(String line) {
            Matcher m = MATCH_ENTRY.matcher(line);
            if (!m.matches()) {
                throw new IllegalArgumentException("invalid concepts entry: '" + line + "'");
            }
            this.phrase = m.group(1);
            this.fraction = Float.valueOf(m.group(2));
            this.article = m.group(3);
            this.flags = m.group(4).trim().split(" ");
        }

        int getNumEnglishLinks() {
            for (String flag : flags) {
                if (flag.startsWith("W:")) {
                    return Fraction.getFraction(flag.substring(2)).getNumerator();
                }
            }
            return 0;
        }
    }

    public static class Provider extends org.wikapidia.conf.Provider<PhraseAnalyzer> {
        public Provider(Configurator configurator, Configuration config) throws ConfigurationException {
            super(configurator, config);
        }

        @Override
        public Class getType() {
            return PhraseAnalyzer.class;
        }

        @Override
        public String getPath() {
            return "phrases.analyzer";
        }

        @Override
        public PhraseAnalyzer get(String name, Config config) throws ConfigurationException {
            if (!config.getString("type").equals("stanford")) {
                return null;
            }
            PhraseAnalyzerDao paDao = getConfigurator().get(PhraseAnalyzerDao.class, config.getString("phraseDao"));
            LocalPageDao lpDao = getConfigurator().get(LocalPageDao.class, config.getString("localPageDao"));
            File path = new File(config.getString("path"));
            return new StanfordPhraseAnalyzer(paDao, lpDao, path);
        }
    }
}

/**
 * MJPEG streaming application.
 * 
 * @author Michael Diponio <michael.diponio@uts.edu.au>
 * @date 18th May 2016
 */

package au.edu.remotelabs.mjpeg;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import au.edu.remotelabs.mjpeg.StreamerConfig.Stream;

/**
 * Servlet to serve MJpeg streams. 
 */
@WebServlet(name="StreamsServlet",
            urlPatterns = "/streams/*", 
            initParams = { @WebInitParam(name = "streams-config", value = "./WebContent/META-INF/streams-config.xml") })
public class StreamerServlet extends HttpServlet 
{
    private static final long serialVersionUID = 1L;
    
    /** Streams. */
    private final Map<String, SourceStream> streams;
    
    /** Streamer configuration. */
    private StreamerConfig config;
    
    /** Logger. */
    private final Logger logger;
    
    public StreamerServlet() 
    {
        super();
        
        this.logger = Logger.getLogger(getClass().getName());
        
        this.streams = new HashMap<>();
    }

    @Override
    public void init(ServletConfig config) throws ServletException 
    {
        String conf = config.getInitParameter("streams-config");
        if (conf == null)
        {
            this.logger.severe("Configuration file for streamer application has not been configured.");
            throw new ServletException("Configuration file location not configured.");
        }
    
        this.config = new StreamerConfig(conf);
        
        for (Stream stream : this.config.getStreams().values())
        {
            this.logger.fine("Loaded configuration for stream: " + stream.name);
            this.streams.put(stream.name, new SourceStream(stream));
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException 
    {
        String url = request.getRequestURI();
        if (url.endsWith(".jpg"))
        {
            SourceStream stream = this.streams.values().iterator().next();
            Frame frame = stream.getLastFrame();
            if (frame != null)
            {
                response.setContentType(frame.getContentType());
                response.setContentLength(frame.getContentLength());
                frame.writeTo(response.getOutputStream());
            }
        }
        
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException 
    {
        doGet(request, response);
    }
    
    @Override
    public void destroy()
    {
        super.destroy();
    }

}
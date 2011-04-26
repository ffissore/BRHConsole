//
//  Copyright (C) 2008 RRD Labs Ltd.  All Rights Reserved.
//
//  This is free software; you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation; either version 2 of the License, or
//  (at your option) any later version.
//
//  This software is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this software; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,
//  USA.
//

import java.applet.*;
import java.net.*;
import java.io.*;
import java.security.*;
import javax.net.ssl.*;

class BRHSocketFactory implements SocketFactory
{
    public Socket createSocket(String host, int port, Applet applet) throws IOException
    {
        return createSocket(host,
                port,
                applet.getParameter("id"),
                applet.getParameter("key"));
    }

    public Socket createSocket(String host, int port, String[] args) throws IOException
    {
        return createSocket(host,
                port,
                readArg(args, "id"),
                readArg(args, "key"));
    }

    public Socket createSocket(String host, int port, String id, String key) throws IOException
    {
        try
        {
            // load the cacerts
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(this.getClass().getResourceAsStream("cacerts"), "123456".toCharArray());

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ks);

            SSLContext ctx = SSLContext.getInstance("SSLv3");
            ctx.init(null, tmf.getTrustManagers(), null);

            SSLSocketFactory ssf = ctx.getSocketFactory();

            // create an ssl connection
            SSLSocket sock = (SSLSocket) ssf.createSocket(host, port);

            // send the id and key, each followed by a newline
            BufferedOutputStream output = new BufferedOutputStream(sock.getOutputStream());
            output.write(id.getBytes());
            output.write('\n');
            output.write(key.getBytes());
            output.write('\n');
            output.flush();

            // now use the socket normally, the server will close the
            // connection if anything went wrong
            return (Socket)sock;
        }
        catch (IOException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new IOException();
        }
    }

    private String readArg(String[] args, String name)
    {
        for (int i = 0; i < args.length - 1; i += 2)
        {
            if (args[i].equalsIgnoreCase(name))
                return args[i+1];
        }

        return null;
    }
}

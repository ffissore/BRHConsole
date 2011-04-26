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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.net.*;
import java.io.*;
import java.util.*;

import org.json.*;


class BRHInit implements ActionListener
{
	VncViewer v = null;
	URL rpc_url = null;
	JTextField email = null;
	JPasswordField password = null;
	String cookie = null;

	String email_arg = null;
	String password_arg = null;

	public void startStandAlone(VncViewer v, String[] argv)
	{
		try
		{
			this.v = v;

			if (haveArg(argv, "-h") || haveArg(argv, "--help"))
				usage();

			String url = readArg(argv, "--url");
			if (url == null)
				url = "https://blueroomhosting.com/rpc/json.pxl";

			email_arg = readArg(argv, "--email");
			password_arg = readArg(argv, "--password");

			rpc_url = new URL(url);

			v.mainArgs = new String[10];

			v.mainArgs[0] = "SocketFactory";
			v.mainArgs[1] = "BRHSocketFactory";

			v.mainArgs[2] = "host";
			v.mainArgs[3] = rpc_url.getHost();

			v.mainArgs[4] = "port";
			v.mainArgs[5] = "5002";

			if (email_arg != null && password_arg != null)
				login(email_arg, password_arg);
			else
				showLoginPrompt();
		}
		catch (Exception e)
		{
			usage();
		}
	}

	public void usage()
	{
		System.out.println("Usage:");
		System.out.println("  java -jar BRHConsole.jar [--url <json-rpc-url>] [--email <email>]");
		System.exit(0);
	}

	public JSONObject json_rpc(String method, JSONArray params) throws Exception
	{
		URLConnection conn = rpc_url.openConnection();
		if (cookie != null)
			conn.addRequestProperty("cookie", cookie);

		JSONObject request = new JSONObject();

		request.put("id", false);
		request.put("method", method);
		request.put("params", params);

		conn.setDoOutput(true);
		OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
		try
		{
			wr.write(request.toString());
			wr.flush();
		}
		finally
		{
			wr.close();
		}

		// Get the response
		InputStreamReader rd = new InputStreamReader(conn.getInputStream());
		try
		{
			JSONTokener tokener = new JSONTokener(rd);
			return (JSONObject)tokener.nextValue();
		}
		finally
		{
			rd.close();
		}
	}

	JFrame login_window = null;
	JButton login_ok, login_cancel;

	public void showLoginPrompt()
	{
		if (login_window == null)
		{
			login_window = new JFrame("BRH Console");
			login_window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

			JPanel top_panel = new JPanel();
			login_window.getContentPane().add(top_panel);

			top_panel.setLayout(new BoxLayout(top_panel, BoxLayout.Y_AXIS));
			top_panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));


			JPanel p = new JPanel();
			top_panel.add(p);
			p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));

			JPanel col_panel = new JPanel();
			p.add(col_panel);
			col_panel.setLayout(new BoxLayout(col_panel, BoxLayout.Y_AXIS));

			col_panel.add(new JLabel("email"));
			col_panel.add(Box.createRigidArea(new Dimension(0,5)));
			col_panel.add(new JLabel("password"));

			p.add(Box.createRigidArea(new Dimension(5,0)));

			col_panel = new JPanel();
			p.add(col_panel);
			col_panel.setLayout(new BoxLayout(col_panel, BoxLayout.Y_AXIS));

			col_panel.add(email = new JTextField(20));
			col_panel.add(Box.createRigidArea(new Dimension(0,5)));
			col_panel.add(password = new JPasswordField());

			if (email_arg != null)
				email.setText(email_arg);
			if (password_arg != null)
				password.setText(password_arg);

			top_panel.add(Box.createRigidArea(new Dimension(0,10)));

			p = new JPanel();
			top_panel.add(p);
			p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));

			p.add(Box.createHorizontalGlue());
			p.add(login_ok = new JButton("OK"));
			p.add(Box.createRigidArea(new Dimension(5,0)));
			p.add(login_cancel = new JButton("Cancel"));
			p.add(Box.createHorizontalGlue());

			login_ok.addActionListener(this); 
			login_cancel.addActionListener(this);

			login_window.pack();
		}

		cookie = null;
		login_window.setVisible(true);
	}

	public void login(String email, String password)
	{
		try
		{
			// call get_login_cookie
			JSONArray params = new JSONArray();
			params.put(email);
			params.put(password);

			JSONObject response = json_rpc("get_login_cookie", params);
			//System.out.println(response.toString());
			cookie = (String)response.get("result");

			// call list_vps
			params = new JSONArray();

			response = json_rpc("list_vps", params);
			//System.out.println(response.toString());
			showVPSPrompt((JSONArray)response.get("result"));
		}
		catch (Exception e)
		{
			System.err.println(e);

			JOptionPane.showMessageDialog(null,
					"Login failed",
					"Login failed",
					JOptionPane.ERROR_MESSAGE);

			showLoginPrompt();
		}
	}

	JFrame vps_list_window = null;
	JList vps_list_box;
	JButton vps_list_ok, vps_list_cancel;
	JSONArray vps_list;

	public void showVPSPrompt(JSONArray vps) throws Exception
	{
		vps_list = vps;

		if (vps_list_window == null)
		{
			vps_list_window = new JFrame("BRH Console");
			vps_list_window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

			JPanel top_panel = new JPanel();
			vps_list_window.getContentPane().add(top_panel);

			top_panel.setLayout(new BoxLayout(top_panel, BoxLayout.Y_AXIS));
			top_panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

			Vector values = new Vector();
			for (int idx=0; idx<vps_list.length(); ++idx)
			{
				JSONArray row = vps_list.getJSONArray(idx);
				values.addElement(row.getString(1));
			}

			vps_list_box = new JList(values);
			top_panel.add(new JScrollPane(vps_list_box));

			vps_list_box.setVisibleRowCount(10);

			JPanel p = new JPanel();
			p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));

			JPanel col_panel = new JPanel();
			p.add(col_panel);
			col_panel.setLayout(new BoxLayout(col_panel, BoxLayout.Y_AXIS));

			col_panel.add(new JLabel("email"));
			col_panel.add(Box.createRigidArea(new Dimension(0,5)));
			col_panel.add(new JLabel("password"));

			p.add(Box.createRigidArea(new Dimension(5,0)));

			col_panel = new JPanel();
			p.add(col_panel);
			col_panel.setLayout(new BoxLayout(col_panel, BoxLayout.Y_AXIS));

			col_panel.add(email = new JTextField(20));
			col_panel.add(Box.createRigidArea(new Dimension(0,5)));
			col_panel.add(password = new JPasswordField());

			top_panel.add(Box.createRigidArea(new Dimension(0,10)));

			p = new JPanel();
			top_panel.add(p);
			p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));

			p.add(Box.createHorizontalGlue());
			p.add(vps_list_ok = new JButton("OK"));
			p.add(Box.createRigidArea(new Dimension(5,0)));
			p.add(vps_list_cancel = new JButton("Cancel"));
			p.add(Box.createHorizontalGlue());

			vps_list_ok.addActionListener(this); 
			vps_list_cancel.addActionListener(this);

			vps_list_window.pack();
		}

		vps_list_window.setVisible(true);
	}

	public void selectVPS(int idx)
	{
		try
		{
			int id = vps_list.getJSONArray(idx).getInt(0);

			// call get_vnc_key
			JSONArray params = new JSONArray();
			params.put(id);

			JSONObject response = json_rpc("get_vnc_key", params);
			//System.out.println(response.toString());

			// set up the last of the parameters
			v.mainArgs[6] = "id";
			v.mainArgs[7] = Integer.toString(id);

			v.mainArgs[8] = "key";
			v.mainArgs[9] = response.getString("result");

			// start VNC
			v.init();
			v.start();
		}
		catch (Exception e)
		{
			System.err.println(e);

			JOptionPane.showMessageDialog(null,
					"Connection failed",
					"Connection failed",
					JOptionPane.ERROR_MESSAGE);

			showLoginPrompt();
		}
	}

	public void actionPerformed(ActionEvent ae)
	{
		if (ae.getSource() == login_ok)
		{
			login_window.setVisible(false);

			login(email.getText(), password.getText());
		}
		else if (ae.getSource() == vps_list_ok)
		{
			int idx = vps_list_box.getSelectedIndex();
			if (idx >= 0)
			{
				vps_list_window.setVisible(false);
				selectVPS(idx);
			}
		}
		else if (ae.getSource() == login_cancel || ae.getSource() == vps_list_cancel)
		{
			System.exit(0);
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

    private boolean haveArg(String[] args, String name)
    {
        for (int i = 0; i < args.length; i += 2)
        {
            if (args[i].equalsIgnoreCase(name))
                return true;
        }

        return false;
    }
}

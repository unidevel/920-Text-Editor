/**
 * Emmet file I/O interface implementation using Java classes 
 * (for Mozilla Rhino)
 *
 * @author Sergey Chikuyonok (serge.che@gmail.com)
 * @link http://chikuyonok.ru
 * @version 0.65
 */
emmet.define('file', function(require, _) {
	function isURL(path) {
		var re = /^https?:\/\//;
		return re.test(String(path));
	}

	return {
		_parseParams: function(args) {
			var params = {
				path: args[0],
				size: 0
			};

			args = _.rest(args);
			params.callback = _.last(args);
			args = _.initial(args);
			if (args.length) {
				params.size = args[0];
			}

			return params;
		},
		
		_read: function(params, isText) {
			var stream = null, content, c;
			if (isURL(params.path)) {
				var url = new java.net.URL(params.path);
				stream = url.openStream();
			} else {
				var f = new java.io.File(params.path);
				if (f.exists() && f.isFile() && f.canRead()) {
					stream = new java.io.FileInputStream(f);
				}
			}

			if (stream) {
				
				if (isText) {
					stream = new java.io.BufferedReader(new java.io.InputStreamReader(stream));
					content = '';
					while( (c = stream.readLine()) != null) {
						content += String(new java.lang.String(c.getBytes(), 'UTF-8'));
					}
				} else {
					content = [];
					while ((c = stream.read()) != -1) {
						content.push(String.fromCharCode(c));
					}
					content = content.join('');
				}

				stream.close();
				return content;
			}
			
			return null;
		},
		
		/**
		 * Read binary file content and return it
		 * @param {String} path File's relative or absolute path
		 * @return {String}
		 * @memberOf __emmetFileJava
		 */
		read: function(path, size, callback) {
			var params = this._parseParams(arguments);
			var content = this._read(params);
			
			if (content) {
				return params.callback(0, content);
			}
			
			params.callback('Unable to read file');
		},
		
		/**
		 * Read file content and return it
		 * @param {String} path File's relative or absolute path
		 * @return {String}
		 * @memberOf __emmetFileJava
		 */
		readText: function(path, size, callback) {
			var params = this._parseParams(arguments);
			var content = this._read(params, true);
			
			if (content) {
				return params.callback(0, content);
			}
			
			params.callback('Unable to read file');
		},

		/**
		 * Locate <code>file_name</code> file that relates to <code>editor_file</code>.
		 * File name may be absolute or relative path
		 *
		 * <b>Dealing with absolute path.</b>
		 * Many modern editors has a "project" support as information unit, but you
		 * should not rely on project path to find file with absolute path. First,
		 * it requires user to create a project before using this method (and this
		 * is not acutually Zen). Second, project path doesn't always points to
		 * to website's document root folder: it may point, for example, to an
		 * upper folder which contains server-side scripts.
		 *
		 * For better result, you should use the following algorithm in locating
		 * absolute resources:
		 * 1) Get parent folder for <code>editor_file</code> as a start point
		 * 2) Append required <code>file_name</code> to start point and test if
		 * file exists
		 * 3) If it doesn't exists, move start point one level up (to parent folder)
		 * and repeat step 2.
		 *
		 * @param {String} editor_file
		 * @param {String} file_name
		 * @return {String|null} Returns null if <code>file_name</code> cannot be located
		 */
		locateFile: function(editor_file, file_name) {
			if (isURL(file_name)) {
				return file_name;
			}

			var File = Packages.java.io.File;
			var f = new File(editor_file),
				result = '',
				tmp;
				
			// traverse upwards to find image uri
			while (f.getParent()) {
				tmp = new File(this.createPath(f.getParent(), file_name));
				if (tmp.exists()) {
					result = tmp.getCanonicalPath();
					break;
				}
				
				f = new File(f.getParent());
			}
			
			return result;
		},

		/**
		 * Creates absolute path by concatenating <code>parent</code> and <code>file_name</code>.
		 * If <code>parent</code> points to file, its parent directory is used
		 * @param {String} parent
		 * @param {String} file_name
		 * @return {String}
		 */
		createPath: function(parent, file_name, callback) {
			var File = Packages.java.io.File,
				f = new File(parent),
				result = '';
				
			if (f.exists()) {
				if (f.isFile()) {
					parent = f.getParent();
				}
				
				var req_file = new File(parent, file_name);
				result = req_file.getCanonicalPath();
			}
			
			if (callback) {
				callback(result);
			}
			
			return result;
		},

		/**
		 * Saves <code>content</code> as <code>file</code>
		 * @param {String} file File's asolute path
		 * @param {String} content File content
		 */
		save: function(file, content) {
			content = content || '';
			file = String(file);
			
			var File = Packages.java.io.File,
				f = new File(file);
				
			if (file.indexOf('/') != -1) {
				var f_parent = new File(f.getParent());
				f_parent.mkdirs();
			}
			
			var stream = new Packages.java.io.FileOutputStream(file);
			for (var i = 0, il = content.length; i < il; i++) {
				stream.write(content.charCodeAt(i));
			}
				
			stream.close();
		},

		/**
		 * Returns file extension in lower case
		 * @param {String} file
		 * @return {String}
		 */
		getExt: function(file) {
			var m = (file || '').match(/\.([\w\-]+)$/);
			return m ? m[1].toLowerCase() : '';
		}
	};
});

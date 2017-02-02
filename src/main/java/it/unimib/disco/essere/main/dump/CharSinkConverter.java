package it.unimib.disco.essere.main.dump;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.beust.jcommander.IStringConverter;
import com.google.common.io.CharSink;
import com.google.common.io.Files;

public class CharSinkConverter implements IStringConverter<CharSink> {

	@Override
	public CharSink convert(String value) {
		if ("-".equals(value)) {
			return new CharSink() {
				@Override
				public Writer openStream() throws IOException {
					return new OutputStreamWriter(System.out,
							StandardCharsets.UTF_8) {
						@Override
						public void close() throws IOException {
							flush();
						}
					};
				}
			};
		} else {
			Path p = Paths.get(value);
			return Files.asCharSink(p.toFile(), StandardCharsets.UTF_8);
		}
	}

}
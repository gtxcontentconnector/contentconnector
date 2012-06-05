package com.gentics.cr.util.velocity;

import org.apache.velocity.tools.generic.AlternatorTool;
import org.apache.velocity.tools.generic.DateTool;
import org.apache.velocity.tools.generic.EscapeTool;
import org.apache.velocity.tools.generic.IteratorTool;
import org.apache.velocity.tools.generic.ListTool;
import org.apache.velocity.tools.generic.MathTool;
import org.apache.velocity.tools.generic.NumberTool;
import org.apache.velocity.tools.generic.RenderTool;
import org.apache.velocity.tools.generic.ResourceTool;
import org.apache.velocity.tools.generic.SortTool;

/**
 * Generic velocity tools in a bean that can be accessed by velocity.
 * @author bigbear3001
 * @see <a href="http://velocity.apache.org/tools/releases/1.3/generic/">Velocity Tools 1.3 Documentation</a>
 */
public class VelocityTools {
	/**
	 * Velocity date tool from velocity tools.
	 */
	private static DateTool date = new DateTool();
	/**
	 * Velocity date tool from velocity tools.
	 */
	private static MathTool math = new MathTool();
	/**
	 * Velocity date tool from velocity tools.
	 */
	private static NumberTool number = new NumberTool();
	/**
	 * Velocity date tool from velocity tools.
	 */
	private static RenderTool render = new RenderTool();
	/**
	 * Velocity date tool from velocity tools.
	 */
	private static EscapeTool esc = new EscapeTool();
	/**
	 * Velocity date tool from velocity tools.
	 */
	private static ResourceTool resource = new ResourceTool();
	/**
	 * Velocity date tool from velocity tools.
	 */
	private static AlternatorTool alternator = new AlternatorTool();
	/**
	 * Velocity date tool from velocity tools.
	 */
	private static ListTool list = new ListTool();
	/**
	 * Velocity date tool from velocity tools.
	 */
	private static SortTool sort = new SortTool();
	/**
	 * Velocity date tool from velocity tools.
	 */
	private static IteratorTool iterator = new IteratorTool();

	//getters for the tools
	/**
	 * @return the date tool
	 */
	public DateTool getDate() {
		return date;
	}

	/**
	 * @return the math tool
	 */
	public MathTool getMath() {
		return math;
	}

	/**
	 * @return the number tool
	 */
	public NumberTool getNumber() {
		return number;
	}

	/**
	 * @return the render tool
	 */
	public RenderTool getRender() {
		return render;
	}

	/**
	 * @return the escape tool
	 */
	public EscapeTool getEsc() {
		return esc;
	}

	/**
	 * @return the resource tool
	 */
	public ResourceTool getResource() {
		return resource;
	}

	/**
	 * @return the alternator tool
	 */
	public AlternatorTool getAlternator() {
		return alternator;
	}

	/**
	 * @return the list tool
	 */
	public ListTool getList() {
		return list;
	}

	/**
	 * @return the sort tool
	 */
	public SortTool getSort() {
		return sort;
	}

	/**
	 * @return the iterator tool
	 */
	public IteratorTool getIterator() {
		return iterator;
	}
}

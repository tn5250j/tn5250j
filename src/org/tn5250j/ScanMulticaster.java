package org.tn5250j;

import org.tn5250j.interfaces.ScanListener;

public class ScanMulticaster implements ScanListener
{
  public static ScanListener add(ScanListener a, ScanListener b)
  {
    if (a == null)  return b;
    if (b == null)  return a;

    return new ScanMulticaster(a, b);
  }

  public static ScanListener remove(ScanListener l, ScanListener oldl)
  {
    return removeInternal(l, oldl);
  }

  protected static ScanListener removeInternal(ScanListener l, ScanListener oldl)
  {
    if (l == null || l == oldl)
    {
    return null;
    }
    else if (l instanceof ScanMulticaster)
    {
      return ((ScanMulticaster)l).remove(oldl);
    }
    else
    {
      return l; // it's not here
    }
  }

  public ScanMulticaster(ScanListener a, ScanListener b)
  {
    this.a = a;
    this.b = b;
  }

  public void scanned(String command, String remainder)
  {
    a.scanned(command, remainder);
    b.scanned(command, remainder);
  }

  protected ScanListener remove(ScanListener old)
  {
    if (old == a)  return b;
    if (old == b)  return a;

    ScanListener a2 = removeInternal(a, old);
    ScanListener b2 = removeInternal(b, old);

    if (a2 == a && b2 == b)
    {
      return this; // it's not here
    }

    return removeInternal(a2, b2);
    }

  private ScanListener a; // = null;
  private ScanListener b; // = null;
}
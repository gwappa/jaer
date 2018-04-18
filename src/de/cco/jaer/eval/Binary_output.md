
To be written:

1. mode (0 for POSITION, 1 for ANGLE)
2. eval (bool to 0 or 1)
3. nevents (3 bytes; the highest byte will be used as mode | eval)
4. firstts (8 bytes)
5. lastts (8 bytes)
6. coords (8 bytes x 2)

and the tracker header part:

```
FastEvent
<data dict offset in long (>8i)>
<data start offset in long (>8i)>
<tracker name>
{   
    "nevents":  ">4i",
    "firstts":  ">8i",
    "lastts":   ">8i",
    <header0>:  "d",
    <header1>:  "d"
}
```

Thus, each packet consists of 36 bytes (compare with the normal output, which can go up to 50 bytes with some buffering and formatting).

